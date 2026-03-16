package com.liv.infra.bbpay.models;

public class SolicitacoesDevedor {

	private int tipoDocumento;
	private long numeroDocumento;
	private int cep;
	private String endereco;
	private String bairro;
	private String cidade;
	private String uf;
	private String email;
	private int dddTelefone;
	private int telefone;
	private long cpfRepresentanteEmpresa;

	public SolicitacoesDevedor() {
	}

	public SolicitacoesDevedor(int tipoDocumento, long numeroDocumento, int cep, String endereco, String bairro,
			String cidade, String uf, String email, int dddTelefone, int telefone, long cpfRepresentanteEmpresa) {
		super();
		this.tipoDocumento = tipoDocumento;
		this.numeroDocumento = numeroDocumento;
		this.cep = cep;
		this.endereco = endereco;
		this.bairro = bairro;
		this.cidade = cidade;
		this.uf = uf;
		this.email = email;
		this.dddTelefone = dddTelefone;
		this.telefone = telefone;
		this.cpfRepresentanteEmpresa = cpfRepresentanteEmpresa;
	}

	public int getTipoDocumento() {
		return tipoDocumento;
	}

	/**
	 * Código do tipo de pessoa do devedor (1 - CPF, 2 - CNPJ). Obrigatório, caso
	 * informado numeroDocumento
	 */
	public void setTipoDocumento(int tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public long getNumeroDocumento() {
		return numeroDocumento;
	}

	/**
	 * Número do CPF/CNPJ do devedor. Obrigatório, caso informado tipoDocumento
	 */
	public void setNumeroDocumento(long numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public int getCep() {
		return cep;
	}

	/**
	 * CEP do devedor
	 */
	public void setCep(int cep) {
		this.cep = cep;
	}

	public String getEndereco() {
		return endereco;
	}

	/**
	 * Texto com o endereço do devedor
	 */
	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getBairro() {
		return bairro;
	}

	/**
	 * Nome do bairro do devedor
	 */
	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	public String getCidade() {
		return cidade;
	}

	/**
	 * Nome da cidade do devedor
	 */
	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	public String getUf() {
		return uf;
	}

	/**
	 * Sigla da Unidade da Federação (sigla do Estado) do devedor
	 */
	public void setUf(String uf) {
		this.uf = uf;
	}

	public String getEmail() {
		return email;
	}

	/**
	 * Texto com o endereço do e-mail do devedor
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	public int getDddTelefone() {
		return dddTelefone;
	}

	/**
	 * Número do DDD do telefone do devedor
	 */
	public void setDddTelefone(int dddTelefone) {
		this.dddTelefone = dddTelefone;
	}

	public int getTelefone() {
		return telefone;
	}

	/**
	 * Número do telefone do devedor
	 */
	public void setTelefone(int telefone) {
		this.telefone = telefone;
	}

	public long getCpfRepresentanteEmpresa() {
		return cpfRepresentanteEmpresa;
	}

	/**
	 * CPF do representante da empresa indicada como devedora
	 */
	public void setCpfRepresentanteEmpresa(long cpfRepresentanteEmpresa) {
		this.cpfRepresentanteEmpresa = cpfRepresentanteEmpresa;
	}

}
