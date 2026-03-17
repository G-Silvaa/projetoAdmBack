package com.liv.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthLoginRequestDTO(
		@NotBlank(message = "Informe o e-mail.")
		@Email(message = "Informe um e-mail válido.")
		String email,
		@NotBlank(message = "Informe a senha.")
		@Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
		String senha
) {
}
