package com.liv.domain;

public enum Status {
	
	AGUARDANDO("Aguardando análise", "Aguardando o processo entrar em análise"),
	PENDENTE("Pendência", "Aguardando alguma documentação"),
	ANALISE("Análise", "Dado entrada no requerimento"),
	CUMPRIMENTO_EXIGENCIA("Cumprimento de exigência", "Anexar alguma documentação"),
	ANALISE_ADMINISTRATIVA("Análise administrativa", "Perícia médica e avaliação social"),
	APROVADO("Aprovado", "resultado final"),
	REPROVADO("Reprovado", "resultado final");
	
	private String nome;
	private String descricao;
	
	Status(String nome, String descricao) {
		this.nome = nome;
		this.descricao = descricao;
	}
	
	public String getNome() {
		return nome;
	}
	
	public String getDescricao() {
		return descricao;
	}

}
