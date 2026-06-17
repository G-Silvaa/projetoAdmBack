package com.liv.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Catálogo de planos do SaaS (compartilhado entre todas as empresas — não é
 * multi-tenant). Os limites null significam "ilimitado".
 */
@Entity
@Table(name = "planos", uniqueConstraints = {
		@UniqueConstraint(name = "planos_uk_codigo", columnNames = "codigo")
})
public class Plano {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "codigo", length = 30, nullable = false)
	private String codigo;

	@Column(name = "nome", length = 60, nullable = false)
	private String nome;

	@Column(name = "preco", precision = 19, scale = 2, nullable = false)
	private BigDecimal preco;

	/** Máximo de usuários do escritório. null = ilimitado. */
	@Column(name = "max_usuarios")
	private Integer maxUsuarios;

	/** Máximo de clientes/segurados cadastrados. null = ilimitado. */
	@Column(name = "max_clientes")
	private Integer maxClientes;

	@Column(name = "ativo", nullable = false)
	private boolean ativo = true;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public BigDecimal getPreco() {
		return preco;
	}

	public void setPreco(BigDecimal preco) {
		this.preco = preco;
	}

	public Integer getMaxUsuarios() {
		return maxUsuarios;
	}

	public void setMaxUsuarios(Integer maxUsuarios) {
		this.maxUsuarios = maxUsuarios;
	}

	public Integer getMaxClientes() {
		return maxClientes;
	}

	public void setMaxClientes(Integer maxClientes) {
		this.maxClientes = maxClientes;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}
}
