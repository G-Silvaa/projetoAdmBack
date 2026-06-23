package com.liv.infra.bbpay.models;

import java.util.List;

public class Vencimento {

	private double multaValorFixo;
	private double jurosPercentual;
	private List<Desconto> descontos;
	private String data;
	private double multaPercentual;

	public Vencimento() {
	}

	public Vencimento(double multaValorFixo, double jurosPercentual, List<Desconto> descontos, String data,
			double multaPercentual) {
		super();
		this.multaValorFixo = multaValorFixo;
		this.jurosPercentual = jurosPercentual;
		this.descontos = descontos;
		this.data = data.toString();
		this.multaPercentual = multaPercentual;
	}

	public double getMultaValorFixo() {
		return multaValorFixo;
	}

	public void setMultaValorFixo(double multaValorFixo) {
		this.multaValorFixo = multaValorFixo;
	}

	public double getJurosPercentual() {
		return jurosPercentual;
	}

	public void setJurosPercentual(double jurosPercentual) {
		this.jurosPercentual = jurosPercentual;
	}

	public List<Desconto> getDescontos() {
		return descontos;
	}

	public void setDescontos(List<Desconto> descontos) {
		this.descontos = descontos;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public double getMultaPercentual() {
		return multaPercentual;
	}

	public void setMultaPercentual(double multaPercentual) {
		this.multaPercentual = multaPercentual;
	}

}
