package com.liv.api.security;

import com.liv.domain.NivelUsuario;
import com.liv.domain.Usuario;

public record AuthenticatedUser(
		Long id,
		Long empresaId,
		String nome,
		String email,
		NivelUsuario nivel
) {

	public static AuthenticatedUser fromEntity(Usuario usuario) {
		return new AuthenticatedUser(
				usuario.getId(),
				usuario.getEmpresaId(),
				usuario.getNome(),
				usuario.getEmail(),
				usuario.getNivel()
		);
	}

	public boolean isAdmin() {
		return nivel == NivelUsuario.ADMINISTRADOR;
	}
}
