package com.liv.domain;

import jakarta.validation.constraints.NotNull;
public class AnoMes {
	
	@NotNull
	private Integer ano;
	
	@NotNull
	private Mes mes;

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public Mes getMes() {
		return mes;
	}

	public void setMes(Mes mes) {
		this.mes = mes;
	}

}
