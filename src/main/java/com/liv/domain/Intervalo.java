package com.liv.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
public class Intervalo {

    @NotNull
    @Valid
    private AnoMes inicio;

    @NotNull
    @Valid
    private AnoMes termino;

	public AnoMes getInicio() {
		return inicio;
	}

	public void setInicio(AnoMes inicio) {
		this.inicio = inicio;
	}

	public AnoMes getTermino() {
		return termino;
	}

	public void setTermino(AnoMes termino) {
		this.termino = termino;
	}

}
