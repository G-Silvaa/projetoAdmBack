package com.liv.infra.bbpay.models;

import java.util.List;

public class Erros {

	private List<ErrInfo> erros;
	private int statusCode;

	public Erros() {
	}

	public Erros(List<ErrInfo> erros, int statusCode) {
		super();
		this.erros = erros;
		this.statusCode = statusCode;
	}

	public List<ErrInfo> getErros() {
		return erros;
	}

	public void setErros(List<ErrInfo> erros) {
		this.erros = erros;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

}
