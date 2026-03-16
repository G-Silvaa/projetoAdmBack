package com.liv.api.dto;

import java.util.Date;

import com.liv.domain.Modalidade;
import com.liv.domain.Status;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessoCessandoDTO {
	
	private String nome;
	private String cpf;
	private String telefone;
	private String numeroProtocolo;
	private Status status;
	private Modalidade beneficio;
	private Date dataConcessao;
	private Date cessacao;
	
	public ProcessoCessandoDTO() {
	}
	
	public ProcessoCessandoDTO(String nome, String cpf, String telefone, String numeroProtocolo, Status status,
			Modalidade beneficio, Date dataConcessao, Date cessacao) {
		super();
		this.nome = nome;
		this.cpf = cpf;
		this.telefone = telefone;
		this.numeroProtocolo = numeroProtocolo;
		this.status = status;
		this.beneficio = beneficio;
		this.dataConcessao = dataConcessao;
		this.cessacao = cessacao;
	}
	
	

}
