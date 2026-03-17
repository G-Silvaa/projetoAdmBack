package com.liv.domain;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "usuarios", uniqueConstraints = {
		@UniqueConstraint(name = "usuarios_uk_email", columnNames = "email")
})
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nome", length = 120, nullable = false)
	private String nome;

	@Column(name = "email", length = 160, nullable = false)
	private String email;

	@Column(name = "senha_hash", length = 120, nullable = false)
	private String senhaHash;

	@Enumerated(EnumType.STRING)
	@Column(name = "nivel", length = 30, nullable = false)
	private NivelUsuario nivel;

	@Column(name = "ativo", nullable = false)
	private boolean ativo = true;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "criado_em", nullable = false)
	private Date criadoEm;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "atualizado_em", nullable = false)
	private Date atualizadoEm;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ultimo_acesso")
	private Date ultimoAcesso;

	@PrePersist
	public void onPrePersist() {
		Date now = new Date();
		criadoEm = now;
		atualizadoEm = now;
		email = email == null ? null : email.trim().toLowerCase();
		nome = nome == null ? null : nome.trim();
	}

	@PreUpdate
	public void onPreUpdate() {
		atualizadoEm = new Date();
		email = email == null ? null : email.trim().toLowerCase();
		nome = nome == null ? null : nome.trim();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public String getSenhaHash() {
		return senhaHash;
	}

	public void setSenhaHash(String senhaHash) {
		this.senhaHash = senhaHash;
	}

	public NivelUsuario getNivel() {
		return nivel;
	}

	public void setNivel(NivelUsuario nivel) {
		this.nivel = nivel;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public Date getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(Date criadoEm) {
		this.criadoEm = criadoEm;
	}

	public Date getAtualizadoEm() {
		return atualizadoEm;
	}

	public void setAtualizadoEm(Date atualizadoEm) {
		this.atualizadoEm = atualizadoEm;
	}

	public Date getUltimoAcesso() {
		return ultimoAcesso;
	}

	public void setUltimoAcesso(Date ultimoAcesso) {
		this.ultimoAcesso = ultimoAcesso;
	}
}
