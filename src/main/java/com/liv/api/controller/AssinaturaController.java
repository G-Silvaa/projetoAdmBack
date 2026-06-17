package com.liv.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liv.api.dto.AssinaturaResponseDTO;
import com.liv.api.security.AuthContext;
import com.liv.api.security.AuthenticatedUser;
import com.liv.domain.SubscriptionService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/assinatura")
public class AssinaturaController {

	private final SubscriptionService subscriptionService;

	public AssinaturaController(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

	@GetMapping
	public ResponseEntity<AssinaturaResponseDTO> minhaAssinatura(HttpServletRequest request) {
		AuthenticatedUser user = AuthContext.requireUser(request);
		return ResponseEntity.ok(subscriptionService.getResumo(user.empresaId()));
	}
}
