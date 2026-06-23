package com.liv.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.liv.api.dto.CobrancaResponseDTO;
import com.liv.infra.abacatepay.AbacatePayClient;
import com.liv.infra.abacatepay.PixCharge;
import com.liv.infra.repository.CobrancaRepository;
import com.liv.infra.repository.EmpresaRepository;

/**
 * Orquestra o pagamento de assinaturas via PIX (AbacatePay). Cria a cobrança,
 * acompanha o status e, ao confirmar o pagamento, ativa a assinatura da empresa.
 */
@Service
public class PagamentoService {

	private final CobrancaRepository cobrancaRepository;
	private final EmpresaRepository empresaRepository;
	private final SubscriptionService subscriptionService;
	private final AbacatePayClient abacatePay;

	public PagamentoService(
			CobrancaRepository cobrancaRepository,
			EmpresaRepository empresaRepository,
			SubscriptionService subscriptionService,
			AbacatePayClient abacatePay
	) {
		this.cobrancaRepository = cobrancaRepository;
		this.empresaRepository = empresaRepository;
		this.subscriptionService = subscriptionService;
		this.abacatePay = abacatePay;
	}

	/** Cria uma cobrança PIX para o plano atual da empresa. */
	@Transactional
	public CobrancaResponseDTO criarCobranca(Long empresaId) {
		Assinatura assinatura = subscriptionService.getAssinatura(empresaId);
		Plano plano = assinatura.getPlano();
		Empresa empresa = empresaRepository.findById(empresaId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada."));

		long centavos = plano.getPreco()
				.multiply(BigDecimal.valueOf(100))
				.setScale(0, RoundingMode.HALF_UP)
				.longValueExact();

		PixCharge charge = abacatePay.criarPixQrCode(
				centavos,
				"Assinatura Arctech - Plano " + plano.getNome(),
				empresa.getNome(),
				empresa.getEmail(),
				empresa.getCnpj(),
				empresa.getTelefone()
		);

		Cobranca cobranca = new Cobranca();
		cobranca.setEmpresaId(empresaId);
		cobranca.setPlano(plano);
		cobranca.setProviderId(charge.providerId());
		cobranca.setValor(plano.getPreco());
		cobranca.setStatus(StatusCobranca.PENDENTE);
		cobranca.setBrCode(charge.brCode());
		cobranca.setBrCodeBase64(charge.brCodeBase64());
		cobranca.setExpiraEm(charge.expiresAt());

		return toDTO(cobrancaRepository.save(cobranca));
	}

	/**
	 * Consulta uma cobrança da empresa. Quando ainda está pendente, confirma o
	 * status no provedor — assim o front pode fazer polling após o pagamento.
	 */
	@Transactional
	public CobrancaResponseDTO consultarCobranca(Long empresaId, Long cobrancaId) {
		Cobranca cobranca = buscar(empresaId, cobrancaId);

		if (cobranca.getStatus() == StatusCobranca.PENDENTE && cobranca.getProviderId() != null) {
			PixCharge charge = abacatePay.consultarPix(cobranca.getProviderId());
			if (charge != null && charge.isPaid()) {
				confirmarPagamento(cobranca);
			}
		}
		return toDTO(cobranca);
	}

	/**
	 * Simula o pagamento de uma cobrança (sandbox). Em produção este endpoint
	 * fica desabilitado; aqui é o gatilho do fluxo de demonstração.
	 */
	@Transactional
	public CobrancaResponseDTO simularPagamento(Long empresaId, Long cobrancaId) {
		Cobranca cobranca = buscar(empresaId, cobrancaId);

		if (cobranca.getStatus() != StatusCobranca.PAGO) {
			if (cobranca.getProviderId() != null) {
				abacatePay.simularPagamento(cobranca.getProviderId());
			}
			confirmarPagamento(cobranca);
		}
		return toDTO(cobranca);
	}

	/** Processa a notificação de pagamento do provedor (webhook). */
	@Transactional
	public void processarWebhook(String providerId, String status) {
		if (providerId == null || !"PAID".equalsIgnoreCase(status)) {
			return;
		}
		cobrancaRepository.findByProviderId(providerId).ifPresent(this::confirmarPagamento);
	}

	private void confirmarPagamento(Cobranca cobranca) {
		if (cobranca.getStatus() == StatusCobranca.PAGO) {
			return;
		}
		cobranca.setStatus(StatusCobranca.PAGO);
		cobranca.setPagoEm(new Date());
		cobrancaRepository.save(cobranca);
		subscriptionService.ativarAssinaturaPaga(cobranca.getEmpresaId());
	}

	private Cobranca buscar(Long empresaId, Long cobrancaId) {
		return cobrancaRepository.findByIdAndEmpresaId(cobrancaId, empresaId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cobrança não encontrada."));
	}

	private CobrancaResponseDTO toDTO(Cobranca cobranca) {
		return new CobrancaResponseDTO(
				cobranca.getId(),
				cobranca.getPlano().getNome(),
				cobranca.getValor(),
				cobranca.getStatus().name(),
				cobranca.getStatus().getLabel(),
				cobranca.getBrCode(),
				cobranca.getBrCodeBase64(),
				cobranca.getExpiraEm(),
				cobranca.getStatus() == StatusCobranca.PAGO
		);
	}
}
