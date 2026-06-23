package com.liv.api.dto;

import com.liv.domain.NivelUsuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequestDTO(
		@Size(max = 120, message = "O nome deve ter até 120 caracteres.")
		String nome,
		@Email(message = "Informe um e-mail válido.")
		String email,
		NivelUsuario nivel,
		Boolean ativo,
		@Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
		String senha
) {
}
