package com.liv.api.dto;

public record AuthResponseDTO(
		String accessToken,
		UsuarioResponseDTO user
) {
}
