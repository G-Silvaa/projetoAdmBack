package com.liv.infra.bbpay.models;

public class Recebedor {

	private int identificadorRecebedor;
	private String tipoRecebedor;
	private double valorRepasse;

	public Recebedor() {
	}

	public Recebedor(int identificadorRecebedor, String tipoRecebedor, double valorRepasse) {
		super();
		this.identificadorRecebedor = identificadorRecebedor;
		this.tipoRecebedor = tipoRecebedor;
		this.valorRepasse = valorRepasse;
	}

	public int getIdentificadorRecebedor() {
		return identificadorRecebedor;
	}

	public void setIdentificadorRecebedor(int identificadorRecebedor) {
		this.identificadorRecebedor = identificadorRecebedor;
	}

	public String getTipoRecebedor() {
		return tipoRecebedor;
	}

	public void setTipoRecebedor(String tipoRecebedor) {
		this.tipoRecebedor = tipoRecebedor;
	}

	public double getValorRepasse() {
		return valorRepasse;
	}

	public void setValorRepasse(double valorRepasse) {
		this.valorRepasse = valorRepasse;
	}

}
