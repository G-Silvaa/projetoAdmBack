package com.liv.api.dto;

import java.util.Date;

import com.liv.domain.NivelUsuario;
import com.liv.domain.Usuario;

public record UsuarioResponseDTO(
		Long id,
		String nome,
		String email,
		NivelUsuario nivel,
		boolean ativo,
		Date criadoEm,
		Date atualizadoEm,
		Date ultimoAcesso
) {

	public static UsuarioResponseDTO fromEntity(Usuario usuario) {
		return new UsuarioResponseDTO(
				usuario.getId(),
				usuario.getNome(),
				usuario.getEmail(),
				usuario.getNivel(),
				usuario.isAtivo(),
				usuario.getCriadoEm(),
				usuario.getAtualizadoEm(),
				usuario.getUltimoAcesso()
		);
	}
}
