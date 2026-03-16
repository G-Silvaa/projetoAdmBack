package com.liv.infra.bbpay.models;

import com.liv.infra.bbpay.models.enums.CodigoTipoPagamento;

public class FormaPagamento {

	private CodigoTipoPagamento codigoTipoPagamento;
	private int quantidadeParcelas;

	public FormaPagamento() {
	}

	public FormaPagamento(CodigoTipoPagamento codigoTipoPagamento, int quantidadeParcelas) {
		super();
		this.codigoTipoPagamento = codigoTipoPagamento;
		this.quantidadeParcelas = quantidadeParcelas;
	}

	public CodigoTipoPagamento getCodigoTipoPagamento() {
		return codigoTipoPagamento;
	}

	public void setCodigoTipoPagamento(CodigoTipoPagamento codigoTipoPagamento) {
		this.codigoTipoPagamento = codigoTipoPagamento;
	}

	public int getQuantidadeParcelas() {
		return quantidadeParcelas;
	}

	public void setQuantidadeParcelas(int quantidadeParcelas) {
		this.quantidadeParcelas = quantidadeParcelas;
	}

}
