package com.liv.infra.bbpay.models;

import java.util.List;

public class SolicitacaoPagamentoRequest {

	private SolicitacaoGeral geral;
	private SolicitacoesDevedor devedor;
	private Vencimento vencimento;
	private List<FormaPagamento> formasPagamento;
	private Repasse repasse;

	public SolicitacaoPagamentoRequest() {
	}

	public SolicitacaoPagamentoRequest(SolicitacaoGeral geral, SolicitacoesDevedor devedor, Vencimento vencimento,
			List<FormaPagamento> formasPagamento, Repasse repasse) {
		super();
		this.geral = geral;
		this.devedor = devedor;
		this.vencimento = vencimento;
		this.formasPagamento = formasPagamento;
		this.repasse = repasse;
	}

	public SolicitacaoPagamentoRequest(SolicitacaoGeral geral, SolicitacoesDevedor devedor, Vencimento vencimento,
			List<FormaPagamento> formasPagamento) {
		super();
		this.geral = geral;
		this.devedor = devedor;
		this.vencimento = vencimento;
		this.formasPagamento = formasPagamento;
	}

	public SolicitacaoGeral getGeral() {
		return geral;
	}

	public void setGeral(SolicitacaoGeral geral) {
		this.geral = geral;
	}

	public SolicitacoesDevedor getDevedor() {
		return devedor;
	}

	public void setDevedor(SolicitacoesDevedor devedor) {
		this.devedor = devedor;
	}

	public Vencimento getVencimento() {
		return vencimento;
	}

	public void setVencimento(Vencimento vencimento) {
		this.vencimento = vencimento;
	}

	public List<FormaPagamento> getFormasPagamento() {
		return formasPagamento;
	}

	public void setFormasPagamento(List<FormaPagamento> formasPagamento) {
		this.formasPagamento = formasPagamento;
	}

	public Repasse getRepasse() {
		return repasse;
	}

	public void setRepasse(Repasse repasse) {
		this.repasse = repasse;
	}

}
