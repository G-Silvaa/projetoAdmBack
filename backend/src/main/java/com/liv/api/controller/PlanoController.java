package com.liv.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liv.api.dto.PlanoResponseDTO;
import com.liv.domain.SubscriptionService;

/** Lista pública de planos (consumida pela landing). */
@RestController
@RequestMapping("/planos")
public class PlanoController {

	private final SubscriptionService subscriptionService;

	public PlanoController(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

	@GetMapping
	public ResponseEntity<List<PlanoResponseDTO>> listar() {
		return ResponseEntity.ok(subscriptionService.listarPlanos());
	}
}
