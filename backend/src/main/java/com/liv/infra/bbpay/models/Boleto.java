package com.liv.infra.bbpay.models;

public class Boleto {

	private String linhaDigitavel;
	private String textoCodigoBarras;

	public Boleto() {
	}

	public Boleto(String linhaDigitavel, String textoCodigoBarras) {
		super();
		this.linhaDigitavel = linhaDigitavel;
		this.textoCodigoBarras = textoCodigoBarras;
	}

	public String getLinhaDigitavel() {
		return linhaDigitavel;
	}

	public void setLinhaDigitavel(String linhaDigitavel) {
		this.linhaDigitavel = linhaDigitavel;
	}

	public String getTextoCodigoBarras() {
		return textoCodigoBarras;
	}

	public void setTextoCodigoBarras(String textoCodigoBarras) {
		this.textoCodigoBarras = textoCodigoBarras;
	}

}
