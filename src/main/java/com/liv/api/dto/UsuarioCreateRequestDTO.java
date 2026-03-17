package com.liv.api.dto;

import com.liv.domain.NivelUsuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioCreateRequestDTO(
		@NotBlank(message = "Informe o nome.")
		@Size(max = 120, message = "O nome deve ter até 120 caracteres.")
		String nome,
		@NotBlank(message = "Informe o e-mail.")
		@Email(message = "Informe um e-mail válido.")
		String email,
		@NotBlank(message = "Informe a senha.")
		@Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
		String senha,
		@NotNull(message = "Informe o nível do usuário.")
		NivelUsuario nivel
) {
}
