package com.liv.api.dto;

import java.util.Date;

import com.liv.domain.Modalidade;
import com.liv.domain.Status;

public class BeneficioConcedidoDTO {
	
	private String nome;
	private String cpf;
	private String telefone;
	private String numeroProtocolo;
	private Status status;
	private Modalidade beneficio;
	private Date dataConcessao;
	
	public BeneficioConcedidoDTO(String nome, String cpf, String telefone, String numeroProtocolo, Status status,
			Modalidade beneficio, Date dataConcessao) {
		super();
		this.nome = nome;
		this.cpf = cpf;
		this.telefone = telefone;
		this.numeroProtocolo = numeroProtocolo;
		this.status = status;
		this.beneficio = beneficio;
		this.dataConcessao = dataConcessao;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getNumeroProtocolo() {
		return numeroProtocolo;
	}

	public void setNumeroProtocolo(String numeroProtocolo) {
		this.numeroProtocolo = numeroProtocolo;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Modalidade getBeneficio() {
		return beneficio;
	}

	public void setBeneficio(Modalidade beneficio) {
		this.beneficio = beneficio;
	}

	public Date getDataConcessao() {
		return dataConcessao;
	}

	public void setDataConcessao(Date dataConcessao) {
		this.dataConcessao = dataConcessao;
	}

}
