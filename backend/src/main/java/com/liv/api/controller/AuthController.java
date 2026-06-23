package com.liv.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liv.api.dto.AuthLoginRequestDTO;
import com.liv.api.dto.AuthRegisterRequestDTO;
import com.liv.api.dto.AuthResponseDTO;
import com.liv.api.dto.UsuarioResponseDTO;
import com.liv.api.security.AuthContext;
import com.liv.domain.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final UsuarioService usuarioService;

	public AuthController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthLoginRequestDTO request) {
		return ResponseEntity.ok(usuarioService.autenticar(request));
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody AuthRegisterRequestDTO request) {
		return ResponseEntity.ok(usuarioService.registrar(request));
	}

	@GetMapping("/me")
	public ResponseEntity<UsuarioResponseDTO> me(HttpServletRequest request) {
		return ResponseEntity.ok(usuarioService.buscarPerfil(AuthContext.requireUser(request).id()));
	}
}
