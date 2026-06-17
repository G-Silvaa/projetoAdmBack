package com.liv.domain;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Tenant do SaaS: cada escritório/cliente que assina o sistema é uma Empresa.
 * Todos os dados de negócio (clientes, contratos, processos, financeiro,
 * usuários) pertencem a uma Empresa e ficam isolados por ela.
 */
@Entity
@Table(name = "empresas")
public class Empresa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nome", length = 160, nullable = false)
	private String nome;

	@Column(name = "cnpj", length = 18)
	private String cnpj;

	@Column(name = "email", length = 160)
	private String email;

	@Column(name = "telefone", length = 20)
	private String telefone;

	// Dados usados na geração de contratos (configuráveis por escritório).
	@Column(name = "endereco", length = 200)
	private String endereco;

	@Column(name = "cidade", length = 80)
	private String cidade;

	@Column(name = "pix_chave", length = 140)
	private String pixChave;

	@Column(name = "dados_bancarios", length = 200)
	private String dadosBancarios;

	@Column(name = "ativo", nullable = false)
	private boolean ativo = true;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "criado_em", nullable = false)
	private Date criadoEm;

	@PrePersist
	public void onPrePersist() {
		if (criadoEm == null) {
			criadoEm = new Date();
		}
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

	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
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

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getCidade() {
		return cidade;
	}

	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	public String getPixChave() {
		return pixChave;
	}

	public void setPixChave(String pixChave) {
		this.pixChave = pixChave;
	}

	public String getDadosBancarios() {
		return dadosBancarios;
	}

	public void setDadosBancarios(String dadosBancarios) {
		this.dadosBancarios = dadosBancarios;
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
}
