package com.liv.infra.bbpay.models;

import java.time.LocalDateTime;

public class SolicitacaoGeral {

	private int numeroConvenio;
	private boolean pagamentoUnico;
	private String timestampLimiteSolicitacao;
	private double valorSolicitacao;
	private String descricaoSolicitacao;
	private String codigoConciliacaoSolicitacao;
	private String urlRetorno;

	public SolicitacaoGeral() {
	}

	public SolicitacaoGeral(int numeroConvenio, boolean pagamentoUnico, LocalDateTime timestampLimiteSolicitacao,
			double valorSolicitacao, String descricaoSolicitacao, String codigoConciliacaoSolicitacao,
			String urlRetorno) {
		super();
		this.numeroConvenio = numeroConvenio;
		this.pagamentoUnico = pagamentoUnico;
		this.timestampLimiteSolicitacao = timestampLimiteSolicitacao.toString();
		this.valorSolicitacao = valorSolicitacao;
		this.descricaoSolicitacao = descricaoSolicitacao;
		this.codigoConciliacaoSolicitacao = codigoConciliacaoSolicitacao;
		this.urlRetorno = urlRetorno;
	}

	public int getNumeroConvenio() {
		return numeroConvenio;
	}

	/**
	 * Número do convênio do cliente no BB Pay
	 */
	public void setNumeroConvenio(int numeroConvenio) {
		this.numeroConvenio = numeroConvenio;
	}

	public boolean isPagamentoUnico() {
		return pagamentoUnico;
	}

	/**
	 * Indica se a solicitação pode ou não ser paga mais de uma vez
	 */
	public void setPagamentoUnico(boolean pagamentoUnico) {
		this.pagamentoUnico = pagamentoUnico;
	}

	public String getTimestampLimiteSolicitacao() {
		return timestampLimiteSolicitacao;
	}

	/**
	 * Data e hora limite máximo para pagamento da solicitação de pagamento
	 * (timestamp). Se não informado, sistema assumirá como 365 dias
	 */
	public void setTimestampLimiteSolicitacao(LocalDateTime timestampLimiteSolicitacao) {
		this.timestampLimiteSolicitacao = timestampLimiteSolicitacao.toString();
	}

	public double getValorSolicitacao() {
		return valorSolicitacao;
	}

	/**
	 * Valor que o cliente deseja receber do pagador, quando não informado será
	 * definido pelo pagador. Obrigatório para pagamentoUnico = true.
	 */
	public void setValorSolicitacao(double valorSolicitacao) {
		this.valorSolicitacao = valorSolicitacao;
	}

	public String getDescricaoSolicitacao() {
		return descricaoSolicitacao;
	}

	/**
	 * Descrição da solicitação do pagamento. Dado que pode estar visível ao pagador
	 */
	public void setDescricaoSolicitacao(String descricaoSolicitacao) {
		this.descricaoSolicitacao = descricaoSolicitacao;
	}

	public String getCodigoConciliacaoSolicitacao() {
		return codigoConciliacaoSolicitacao;
	}

	/**
	 * Funciona como um identificador gerenciado por quem requisita a criação do
	 * recurso
	 */
	public void setCodigoConciliacaoSolicitacao(String codigoConciliacaoSolicitacao) {
		this.codigoConciliacaoSolicitacao = codigoConciliacaoSolicitacao;
	}

	public String getUrlRetorno() {
		return urlRetorno;
	}

	/**
	 * URL na qual o cliente será redirecionado após a conclusão do pagamento
	 */
	public void setUrlRetorno(String urlRetorno) {
		this.urlRetorno = urlRetorno;
	}

}
