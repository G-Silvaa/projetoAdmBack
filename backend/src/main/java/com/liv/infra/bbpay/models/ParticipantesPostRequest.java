package com.liv.infra.bbpay.models;

public class ParticipantesPostRequest {

	public int numeroConvenio;
	public String nomeParticipante;
	public int tipoDocumento;
	public long numeroDocumento;
	public int numeroConta;
	public int numeroAgencia;
	public int tipoConta;
	public int variacaoPoupanca;
	public int codigoIspb;

	public ParticipantesPostRequest() {
	}

	public ParticipantesPostRequest(int numeroConvenio, String nomeParticipante, int tipoDocumento,
			long numeroDocumento,
			int numeroConta, int numeroAgencia, int tipoConta, int variacaoPoupanca, int codigoIspb) {
		super();
		this.numeroConvenio = numeroConvenio;
		this.nomeParticipante = nomeParticipante;
		this.tipoDocumento = tipoDocumento;
		this.numeroDocumento = numeroDocumento;
		this.numeroConta = numeroConta;
		this.numeroAgencia = numeroAgencia;
		this.tipoConta = tipoConta;
		this.variacaoPoupanca = variacaoPoupanca;
		this.codigoIspb = codigoIspb;
	}

	public int getNumeroConvenio() {
		return numeroConvenio;
	}

	/**
	 * Número do convênio do cliente no BB Pay
	 */
	public void setNumeroConvenio(int numeroConvenio) {
		this.numeroConvenio = numeroConvenio;
	}

	public String getNomeParticipante() {
		return nomeParticipante;
	}

	/**
	 * Nome identificador do Participante para controle do Cliente.
	 */
	public void setNomeParticipante(String nomeParticipante) {
		this.nomeParticipante = nomeParticipante;
	}

	public int getTipoDocumento() {
		return tipoDocumento;
	}

	/**
	 * Código do tipo de pessoa do Participante (1 - CPF, 2 - CNPJ)
	 */
	public void setTipoDocumento(int tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public long getNumeroDocumento() {
		return numeroDocumento;
	}

	/**
	 * Número do CPF/CNPJ do Participante
	 */
	public void setNumeroDocumento(long numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public int getNumeroConta() {
		return numeroConta;
	}

	/**
	 * Número da Conta utilizada para fazer o repasse para o Participante
	 */
	public void setNumeroConta(int numeroConta) {
		this.numeroConta = numeroConta;
	}

	public int getNumeroAgencia() {
		return numeroAgencia;
	}

	/**
	 * Número da Agência (sem dígito verificador) utilizada para fazer o repasse
	 * para o Participante
	 */
	public void setNumeroAgencia(int numeroAgencia) {
		this.numeroAgencia = numeroAgencia;
	}

	public int getTipoConta() {
		return tipoConta;
	}

	/**
	 * Código do tipo de conta do repasse do participante. As opções disponiveis
	 * são: (1 - Conta Corrente, 2 - Conta Poupança, 3 - Conta Salario 4 - Conta
	 * pré-paga)
	 */
	public void setTipoConta(int tipoConta) {
		this.tipoConta = tipoConta;
	}

	public int getVariacaoPoupanca() {
		return variacaoPoupanca;
	}

	/**
	 * Valor de variação da poupança. Deve ser informado se tipo de conta for
	 * Poupança
	 */
	public void setVariacaoPoupanca(int variacaoPoupanca) {
		this.variacaoPoupanca = variacaoPoupanca;
	}

	public int getCodigoIspb() {
		return codigoIspb;
	}

	/**
	 * Código identificador do Sistema de Pagamentos Brasileiro. Atualmente
	 * aceitamos apenas Banco do Brasil, codigoIspb igual a 0
	 */
	public void setCodigoIspb(int codigoIspb) {
		this.codigoIspb = codigoIspb;
	}

}
