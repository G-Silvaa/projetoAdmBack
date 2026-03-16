package com.liv.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Contato {
	
	@NotBlank(message = "Informe o nome do contato")
	@Column(name = "nome", length = 100, nullable = false)
	private String nome;
	
	@Email
    @Column(name = "email", length = 60)
	private String email;
	
	@Column(name = "telefone", length = 11)
	private String telefone;

}
