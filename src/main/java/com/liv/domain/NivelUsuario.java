package com.liv.domain;

public enum NivelUsuario {

	ADMINISTRADOR("Administrador"),
	GESTOR("Gestor"),
	OPERADOR("Operador");

	private final String label;

	NivelUsuario(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
