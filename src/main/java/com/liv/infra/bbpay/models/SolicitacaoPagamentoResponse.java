package com.liv.infra.bbpay.models;

public class SolicitacaoPagamentoResponse {

	private int numeroSolicitacao;
	private String timestampCriacaoSolicitacao;
	private String urlSolicitacao;
	private Boleto informacoesBoleto;
	private Pix informacoesPIX;
	private Erros erros;

	public SolicitacaoPagamentoResponse() {
	}

	public SolicitacaoPagamentoResponse(int numeroSolicitacao, String timestampCriacaoSolicitacao,
			String urlSolicitacao, Boleto informacoesBoleto, Pix informacoesPIX, Erros erros) {
		super();
		this.numeroSolicitacao = numeroSolicitacao;
		this.timestampCriacaoSolicitacao = timestampCriacaoSolicitacao;
		this.urlSolicitacao = urlSolicitacao;
		this.informacoesBoleto = informacoesBoleto;
		this.informacoesPIX = informacoesPIX;
		this.erros = erros;
	}

	public int getNumeroSolicitacao() {
		return numeroSolicitacao;
	}

	public void setNumeroSolicitacao(int numeroSolicitacao) {
		this.numeroSolicitacao = numeroSolicitacao;
	}

	public String getTimestampCriacaoSolicitacao() {
		return timestampCriacaoSolicitacao;
	}

	public void setTimestampCriacaoSolicitacao(String timestampCriacaoSolicitacao) {
		this.timestampCriacaoSolicitacao = timestampCriacaoSolicitacao;
	}

	public String getUrlSolicitacao() {
		return urlSolicitacao;
	}

	public void setUrlSolicitacao(String urlSolicitacao) {
		this.urlSolicitacao = urlSolicitacao;
	}

	public Boleto getInformacoesBoleto() {
		return informacoesBoleto;
	}

	public void setInformacoesBoleto(Boleto informacoesBoleto) {
		this.informacoesBoleto = informacoesBoleto;
	}

	public Pix getInformacoesPIX() {
		return informacoesPIX;
	}

	public void setInformacoesPIX(Pix informacoesPIX) {
		this.informacoesPIX = informacoesPIX;
	}

	public Erros getErros() {
		return erros;
	}

	public void setErros(Erros erros) {
		this.erros = erros;
	}

}
