package com.liv.infra.bbpay.models;

public class ErrInfo {

	private String codigo;
	private String versao;
	private String mensagem;
	private String ocorrencia;

	public ErrInfo() {
	}

	public ErrInfo(String codigo, String versao, String mensagem, String ocorrencia) {
		super();
		this.codigo = codigo;
		this.versao = versao;
		this.mensagem = mensagem;
		this.ocorrencia = ocorrencia;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getVersao() {
		return versao;
	}

	public void setVersao(String versao) {
		this.versao = versao;
	}

	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	public String getOcorrencia() {
		return ocorrencia;
	}

	public void setOcorrencia(String ocorrencia) {
		this.ocorrencia = ocorrencia;
	}

}
