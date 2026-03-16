package com.liv.infra.bbpay.models;

import java.time.LocalDate;

public class Desconto {

	private String dataLimite;
	private double valorFixo;
	private double valorPercentual;

	public Desconto() {
	}

	public Desconto(LocalDate dataLimite, double valorFixo, double valorPercentual) {
		super();
		this.dataLimite = dataLimite.toString();
		this.valorFixo = valorFixo;
		this.valorPercentual = valorPercentual;
	}

	public String getDataLimite() {
		return dataLimite;
	}

	public void setDataLimite(LocalDate dataLimite) {
		this.dataLimite = dataLimite.toString();
	}

	public double getValorFixo() {
		return valorFixo;
	}

	public void setValorFixo(double valorFixo) {
		this.valorFixo = valorFixo;
	}

	public double getValorPercentual() {
		return valorPercentual;
	}

	public void setValorPercentual(double valorPercentual) {
		this.valorPercentual = valorPercentual;
	}

}
