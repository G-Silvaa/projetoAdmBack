package com.liv.infra.bbpay.models;

import java.util.List;

public class Repasse {

	private String tipoValorRepasse;
	private List<Recebedor> recebedores;

	public Repasse() {
	}

	public Repasse(String tipoValorRepasse, List<Recebedor> recebedores) {
		super();
		this.tipoValorRepasse = tipoValorRepasse;
		this.recebedores = recebedores;
	}

	public String getTipoValorRepasse() {
		return tipoValorRepasse;
	}

	public void setTipoValorRepasse(String tipoValorRepasse) {
		this.tipoValorRepasse = tipoValorRepasse;
	}

	public List<Recebedor> getRecebedores() {
		return recebedores;
	}

	public void setRecebedores(List<Recebedor> recebedores) {
		this.recebedores = recebedores;
	}

}
