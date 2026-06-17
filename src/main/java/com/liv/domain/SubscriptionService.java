package com.liv.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.liv.api.dto.AssinaturaResponseDTO;
import com.liv.api.dto.PlanoResponseDTO;
import com.liv.infra.repository.AssinaturaRepository;
import com.liv.infra.repository.PlanoRepository;
import com.liv.infra.repository.UsuarioRepository;

/**
 * Regras de assinatura do SaaS: criação do trial no cadastro, verificação de
 * acesso (para o "gate") e enforcement dos limites de cada plano.
 */
@Service
public class SubscriptionService {

	/** Dias de teste grátis concedidos no cadastro. */
	public static final int TRIAL_DIAS = 7;
	/** Plano usado durante o trial quando nenhum é informado. */
	public static final String PLANO_TRIAL_PADRAO = "profissional";

	private final AssinaturaRepository assinaturaRepository;
	private final PlanoRepository planoRepository;
	private final UsuarioRepository usuarioRepository;

	public SubscriptionService(
			AssinaturaRepository assinaturaRepository,
			PlanoRepository planoRepository,
			UsuarioRepository usuarioRepository
	) {
		this.assinaturaRepository = assinaturaRepository;
		this.planoRepository = planoRepository;
		this.usuarioRepository = usuarioRepository;
	}

	/** Cria a assinatura em período de teste para uma empresa recém-cadastrada. */
	@Transactional
	public Assinatura criarTrial(Long empresaId, String planoCodigo) {
		String codigo = (planoCodigo == null || planoCodigo.isBlank()) ? PLANO_TRIAL_PADRAO : planoCodigo;
		Plano plano = planoRepository.findByCodigo(codigo)
				.or(() -> planoRepository.findByCodigo(PLANO_TRIAL_PADRAO))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
						"Nenhum plano cadastrado para iniciar a assinatura."));

		Date agora = new Date();
		Date fimTrial = somarDias(agora, TRIAL_DIAS);

		Assinatura assinatura = new Assinatura();
		assinatura.setEmpresaId(empresaId);
		assinatura.setPlano(plano);
		assinatura.setStatus(StatusAssinatura.TRIAL);
		assinatura.setInicio(agora);
		assinatura.setTrialAte(fimTrial);
		assinatura.setVencimento(fimTrial);

		return assinaturaRepository.save(assinatura);
	}

	@Transactional(readOnly = true)
	public Assinatura getAssinatura(Long empresaId) {
		return assinaturaRepository.findByEmpresaId(empresaId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Assinatura não encontrada para a empresa."));
	}

	/** Resumo da assinatura da empresa, para a tela de billing e o gate no front. */
	@Transactional(readOnly = true)
	public AssinaturaResponseDTO getResumo(Long empresaId) {
		Assinatura a = getAssinatura(empresaId);
		Plano p = a.getPlano();

		Integer diasRestantes = null;
		if (a.getVencimento() != null) {
			long ms = a.getVencimento().getTime() - new Date().getTime();
			diasRestantes = ms <= 0 ? 0 : (int) (ms / (24L * 60 * 60 * 1000)) + 1;
		}

		return new AssinaturaResponseDTO(
				p.getCodigo(),
				p.getNome(),
				p.getPreco(),
				a.getStatus().name(),
				a.getStatus().getLabel(),
				a.getTrialAte(),
				a.getVencimento(),
				a.permiteAcesso(),
				diasRestantes,
				p.getMaxUsuarios(),
				p.getMaxClientes()
		);
	}

	/** Lista os planos ativos (usado na landing/escolha de plano). */
	@Transactional(readOnly = true)
	public List<PlanoResponseDTO> listarPlanos() {
		return planoRepository.findAllByAtivoTrueOrderByPrecoAsc()
				.stream()
				.map(PlanoResponseDTO::fromEntity)
				.toList();
	}

	/**
	 * Diz se a empresa pode usar o sistema agora. Fail-open quando não há
	 * assinatura cadastrada (evita travar contas durante a transição).
	 */
	@Transactional(readOnly = true)
	public boolean permiteAcesso(Long empresaId) {
		return assinaturaRepository.findByEmpresaId(empresaId)
				.map(Assinatura::permiteAcesso)
				.orElse(true);
	}

	/** Bloqueia a criação de mais usuários do que o plano permite. */
	@Transactional(readOnly = true)
	public void assertDentroDoLimiteDeUsuarios(Long empresaId) {
		assinaturaRepository.findByEmpresaId(empresaId).ifPresent(assinatura -> {
			Integer max = assinatura.getPlano().getMaxUsuarios();
			if (max == null) {
				return;
			}
			long atual = usuarioRepository.countByEmpresaId(empresaId);
			if (atual >= max) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN,
						"Seu plano permite até " + max + " usuário(s). Faça upgrade para adicionar mais.");
			}
		});
	}

	/** Bloqueia o cadastro de mais clientes do que o plano permite. */
	@Transactional(readOnly = true)
	public void assertDentroDoLimiteDeClientes(Long empresaId, long quantidadeAtual) {
		assinaturaRepository.findByEmpresaId(empresaId).ifPresent(assinatura -> {
			Integer max = assinatura.getPlano().getMaxClientes();
			if (max == null) {
				return;
			}
			if (quantidadeAtual >= max) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN,
						"Seu plano permite até " + max + " clientes. Faça upgrade para cadastrar mais.");
			}
		});
	}

	private static Date somarDias(Date base, int dias) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(base);
		calendar.add(Calendar.DAY_OF_MONTH, dias);
		return calendar.getTime();
	}
}
