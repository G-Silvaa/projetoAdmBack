package com.liv.infra.bbpay.models;

import com.liv.infra.bbpay.models.enums.EstadoSolicitacao;

public class GetSolicitacaoResponse {

	private long numeroSolicitacao;
	private String descricaoSolicitacao;
	private int codigoEstadoSolicitacao;
	private String timestampCriacaoSolicitacao;
	private String timestampLimiteSolicitacao;
	private String urlSolicitacao;
	private int quantidadePagamentosEfetivados;
	private double valorSomatorioPagamentosEfetivados;
	private int quantidadeDevolucoes;
	private double valorSomatorioDevolucoes;
	private boolean pagamentoUnico;
	private double valorSolicitacao;
	private String codigoConciliacaoSolicitacao;
	private Pix informacoesPix;
	private Boleto informacoesBoleto;
	private Vencimento vencimento;
	private Repasse repasse;

	public GetSolicitacaoResponse() {
	}

	public GetSolicitacaoResponse(long numeroSolicitacao, String descricaoSolicitacao, int codigoEstadoSolicitacao,
			String timestampCriacaoSolicitacao, String timestampLimiteSolicitacao, String urlSolicitacao,
			int quantidadePagamentosEfetivados, double valorSomatorioPagamentosEfetivados, int quantidadeDevolucoes,
			double valorSomatorioDevolucoes, boolean pagamentoUnico, double valorSolicitacao,
			String codigoConciliacaoSolicitacao, Pix informacoesPix, Boleto informacoesBoleto, Vencimento vencimento,
			Repasse repasse) {
		super();
		this.numeroSolicitacao = numeroSolicitacao;
		this.descricaoSolicitacao = descricaoSolicitacao;
		this.codigoEstadoSolicitacao = codigoEstadoSolicitacao;
		this.timestampCriacaoSolicitacao = timestampCriacaoSolicitacao;
		this.timestampLimiteSolicitacao = timestampLimiteSolicitacao;
		this.urlSolicitacao = urlSolicitacao;
		this.quantidadePagamentosEfetivados = quantidadePagamentosEfetivados;
		this.valorSomatorioPagamentosEfetivados = valorSomatorioPagamentosEfetivados;
		this.quantidadeDevolucoes = quantidadeDevolucoes;
		this.valorSomatorioDevolucoes = valorSomatorioDevolucoes;
		this.pagamentoUnico = pagamentoUnico;
		this.valorSolicitacao = valorSolicitacao;
		this.codigoConciliacaoSolicitacao = codigoConciliacaoSolicitacao;
		this.informacoesPix = informacoesPix;
		this.informacoesBoleto = informacoesBoleto;
		this.vencimento = vencimento;
		this.repasse = repasse;
	}

	public long getNumeroSolicitacao() {
		return numeroSolicitacao;
	}

	public void setNumeroSolicitacao(long numeroSolicitacao) {
		this.numeroSolicitacao = numeroSolicitacao;
	}

	public String getDescricaoSolicitacao() {
		return descricaoSolicitacao;
	}

	public void setDescricaoSolicitacao(String descricaoSolicitacao) {
		this.descricaoSolicitacao = descricaoSolicitacao;
	}

	public EstadoSolicitacao getCodigoEstadoSolicitacao() {
		switch (this.codigoEstadoSolicitacao) {
			case 0:
				return EstadoSolicitacao.AGUARDANDO_PAGAMENTO;
			case 1:
				return EstadoSolicitacao.PAGA;
			case 800:
				return EstadoSolicitacao.EXPIRADA;
			case 850:
				return EstadoSolicitacao.ABANDONADA;
			case 900:
				return EstadoSolicitacao.EXCLUIDA;
		}

		return null;
	}

	public void setCodigoEstadoSolicitacao(int codigoEstadoSolicitacao) {
		this.codigoEstadoSolicitacao = codigoEstadoSolicitacao;
	}

	public String getTimestampCriacaoSolicitacao() {
		return timestampCriacaoSolicitacao;
	}

	public void setTimestampCriacaoSolicitacao(String timestampCriacaoSolicitacao) {
		this.timestampCriacaoSolicitacao = timestampCriacaoSolicitacao;
	}

	public String getTimestampLimiteSolicitacao() {
		return timestampLimiteSolicitacao;
	}

	public void setTimestampLimiteSolicitacao(String timestampLimiteSolicitacao) {
		this.timestampLimiteSolicitacao = timestampLimiteSolicitacao;
	}

	public String getUrlSolicitacao() {
		return urlSolicitacao;
	}

	public void setUrlSolicitacao(String urlSolicitacao) {
		this.urlSolicitacao = urlSolicitacao;
	}

	public int getQuantidadePagamentosEfetivados() {
		return quantidadePagamentosEfetivados;
	}

	public void setQuantidadePagamentosEfetivados(int quantidadePagamentosEfetivados) {
		this.quantidadePagamentosEfetivados = quantidadePagamentosEfetivados;
	}

	public double getValorSomatorioPagamentosEfetivados() {
		return valorSomatorioPagamentosEfetivados;
	}

	public void setValorSomatorioPagamentosEfetivados(double valorSomatorioPagamentosEfetivados) {
		this.valorSomatorioPagamentosEfetivados = valorSomatorioPagamentosEfetivados;
	}

	public int getQuantidadeDevolucoes() {
		return quantidadeDevolucoes;
	}

	public void setQuantidadeDevolucoes(int quantidadeDevolucoes) {
		this.quantidadeDevolucoes = quantidadeDevolucoes;
	}

	public double getValorSomatorioDevolucoes() {
		return valorSomatorioDevolucoes;
	}

	public void setValorSomatorioDevolucoes(double valorSomatorioDevolucoes) {
		this.valorSomatorioDevolucoes = valorSomatorioDevolucoes;
	}

	public boolean isPagamentoUnico() {
		return pagamentoUnico;
	}

	public void setPagamentoUnico(boolean pagamentoUnico) {
		this.pagamentoUnico = pagamentoUnico;
	}

	public double getValorSolicitacao() {
		return valorSolicitacao;
	}

	public void setValorSolicitacao(double valorSolicitacao) {
		this.valorSolicitacao = valorSolicitacao;
	}

	public String getCodigoConciliacaoSolicitacao() {
		return codigoConciliacaoSolicitacao;
	}

	public void setCodigoConciliacaoSolicitacao(String codigoConciliacaoSolicitacao) {
		this.codigoConciliacaoSolicitacao = codigoConciliacaoSolicitacao;
	}

	public Pix getInformacoesPix() {
		return informacoesPix;
	}

	public void setInformacoesPix(Pix informacoesPix) {
		this.informacoesPix = informacoesPix;
	}

	public Boleto getInformacoesBoleto() {
		return informacoesBoleto;
	}

	public void setInformacoesBoleto(Boleto informacoesBoleto) {
		this.informacoesBoleto = informacoesBoleto;
	}

	public Vencimento getVencimento() {
		return vencimento;
	}

	public void setVencimento(Vencimento vencimento) {
		this.vencimento = vencimento;
	}

	public Repasse getRepasse() {
		return repasse;
	}

	public void setRepasse(Repasse repasse) {
		this.repasse = repasse;
	}

}
