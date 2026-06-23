package com.liv.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Modalidade {

	BPC_LOAS__DEFICIENTE("Amparo Assistencial ao Portador de Deficiência (LOAS)", "87"),
	BPC_LOAS__IDOSO("Amparo Assistencial ao Idoso (LOAS)", "88"),
	APOSENTADORIA_IDADE("Aposentadoria por idade", "41"),
	APOSENTADORIA_TEMPO_CONTRIBUICAO("Aposentadoria por Tempo de Contribuição Previdenciária", "42"),
	APOSENTADORIA_INVALIDEZ("Aposentadoria por Invalidez Previdenciária", "32"),
	PENSAO_MORTE("Pensão por Morte Previdenciária", "21"),
	AUXILIO_RECLUSAO("Auxílio Reclusão", "25"),
	AUXILIO_INCAPACIDADE_TEMPORARIA("Auxílio por Incapacidade Temporária", "31"),
	AUXILIO_ACIDENTE("Auxílio Acidente", "36"),
	SALARIO_MATERNIDADE("Salário Maternidade", "80");

	private String descricao;
	private String modalidade;

	Modalidade(String descricao, String modalidade) {
		this.descricao = descricao;
		this.modalidade = modalidade;
	}

	@JsonCreator
	public static Modalidade fromBeneficio(String beneficio) {
		for (Modalidade modalidade : Modalidade.values()) {
			if (modalidade.getModalidade().equals(beneficio)) {
				return modalidade;
			}
		}

		throw new IllegalArgumentException("Benefício não encontrado: " + beneficio);
	}

	public String getDescricao() {
		return descricao;
	}

	@JsonValue
	public String getModalidade() {
		return modalidade;
	}

}
