package com.liv.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

}
