package com.liv.api.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.liv.api.dto.AssinaturaResponseDTO;
import com.liv.api.dto.CobrancaResponseDTO;
import com.liv.api.security.AuthContext;
import com.liv.api.security.AuthenticatedUser;
import com.liv.domain.PagamentoService;
import com.liv.domain.SubscriptionService;
import com.liv.infra.abacatepay.AbacatePayProperties;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/assinatura")
public class AssinaturaController {

	private final SubscriptionService subscriptionService;
	private final PagamentoService pagamentoService;
	private final AbacatePayProperties abacatePayProperties;

	public AssinaturaController(
			SubscriptionService subscriptionService,
			PagamentoService pagamentoService,
			AbacatePayProperties abacatePayProperties
	) {
		this.subscriptionService = subscriptionService;
		this.pagamentoService = pagamentoService;
		this.abacatePayProperties = abacatePayProperties;
	}

	@GetMapping
	public ResponseEntity<AssinaturaResponseDTO> minhaAssinatura(HttpServletRequest request) {
		AuthenticatedUser user = AuthContext.requireUser(request);
		return ResponseEntity.ok(subscriptionService.getResumo(user.empresaId()));
	}

	/** Gera uma cobrança PIX para o plano atual da empresa. */
	@PostMapping("/pagamento")
	public ResponseEntity<CobrancaResponseDTO> criarPagamento(HttpServletRequest request) {
		AuthenticatedUser user = AuthContext.requireUser(request);
		return ResponseEntity.ok(pagamentoService.criarCobranca(user.empresaId()));
	}

	/** Consulta o status de uma cobrança (polling do front). */
	@GetMapping("/pagamento/{id}")
	public ResponseEntity<CobrancaResponseDTO> consultarPagamento(
			HttpServletRequest request,
			@PathVariable Long id
	) {
		AuthenticatedUser user = AuthContext.requireUser(request);
		return ResponseEntity.ok(pagamentoService.consultarCobranca(user.empresaId(), id));
	}

	/** Simula a confirmação do pagamento (sandbox). */
	@PostMapping("/pagamento/{id}/simular")
	public ResponseEntity<CobrancaResponseDTO> simularPagamento(
			HttpServletRequest request,
			@PathVariable Long id
	) {
		AuthenticatedUser user = AuthContext.requireUser(request);
		return ResponseEntity.ok(pagamentoService.simularPagamento(user.empresaId(), id));
	}

	/** Webhook de confirmação do AbacatePay (rota pública, autenticada por segredo). */
	@PostMapping("/webhook")
	public ResponseEntity<Map<String, String>> webhook(
			@RequestParam(name = "webhookSecret", required = false) String webhookSecret,
			@RequestBody JsonNode payload
	) {
		String esperado = abacatePayProperties.getWebhookSecret();
		if (esperado != null && !esperado.isBlank() && !esperado.equals(webhookSecret)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Segredo inválido."));
		}

		JsonNode data = payload.path("data");
		JsonNode charge = data.has("pixQrCode") ? data.path("pixQrCode")
				: data.has("billing") ? data.path("billing")
				: data;

		String providerId = charge.path("id").asText(null);
		String status = charge.path("status").asText(null);
		if (status == null && payload.path("event").asText("").toLowerCase().endsWith("paid")) {
			status = "PAID";
		}

		pagamentoService.processarWebhook(providerId, status);
		return ResponseEntity.ok(Map.of("message", "ok"));
	}
}
