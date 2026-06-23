package com.liv.infra.bbpay.models;

public class ParticipantesPostResponse {

	public int numeroParticipante;
	private Erros erros;

	public ParticipantesPostResponse() {
	}

	public ParticipantesPostResponse(int numeroParticipante) {
		this.numeroParticipante = numeroParticipante;
	}

	public int getNumeroParticipante() {
		return this.numeroParticipante;
	}

	public void setNumeroParticipante(int numeroParticipante) {
		this.numeroParticipante = numeroParticipante;
	}

	public Erros getErros() {
		return erros;
	}

	public void setErros(Erros erros) {
		this.erros = erros;
	}

}
