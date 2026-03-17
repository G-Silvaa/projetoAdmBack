package com.liv.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liv.api.dto.NivelUsuarioDTO;
import com.liv.api.dto.UsuarioCreateRequestDTO;
import com.liv.api.dto.UsuarioResponseDTO;
import com.liv.api.dto.UsuarioUpdateRequestDTO;
import com.liv.api.security.AuthContext;
import com.liv.api.security.AuthenticatedUser;
import com.liv.domain.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping
	public ResponseEntity<List<UsuarioResponseDTO>> listar(HttpServletRequest request) {
		AuthContext.requireAdmin(request);
		return ResponseEntity.ok(usuarioService.listarUsuarios());
	}

	@GetMapping("/niveis")
	public ResponseEntity<List<NivelUsuarioDTO>> listarNiveis(HttpServletRequest request) {
		AuthContext.requireAdmin(request);
		return ResponseEntity.ok(usuarioService.listarNiveis());
	}

	@PostMapping
	public ResponseEntity<UsuarioResponseDTO> criar(
			@Valid @RequestBody UsuarioCreateRequestDTO request,
			HttpServletRequest httpServletRequest
	) {
		AuthContext.requireAdmin(httpServletRequest);
		return ResponseEntity.ok(usuarioService.criarUsuario(request));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<UsuarioResponseDTO> atualizar(
			@PathVariable Long id,
			@Valid @RequestBody UsuarioUpdateRequestDTO request,
			HttpServletRequest httpServletRequest
	) {
		AuthenticatedUser authenticatedUser = AuthContext.requireAdmin(httpServletRequest);
		return ResponseEntity.ok(usuarioService.atualizarUsuario(id, request, authenticatedUser));
	}
}
