package com.liv.api.security;

import com.liv.domain.NivelUsuario;
import com.liv.domain.Usuario;

public record AuthenticatedUser(
		Long id,
		String nome,
		String email,
		NivelUsuario nivel
) {

	public static AuthenticatedUser fromEntity(Usuario usuario) {
		return new AuthenticatedUser(
				usuario.getId(),
				usuario.getNome(),
				usuario.getEmail(),
				usuario.getNivel()
		);
	}

	public boolean isAdmin() {
		return nivel == NivelUsuario.ADMINISTRADOR;
	}
}
