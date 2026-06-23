package com.liv.domain;

public enum StatusAssinatura {

	/** Período de teste grátis. */
	TRIAL("Em teste grátis"),
	/** Assinatura paga e em dia. */
	ATIVA("Ativa"),
	/** Pagamento em atraso (acesso ainda permitido por um período de tolerância). */
	INADIMPLENTE("Pagamento pendente"),
	/** Cancelada pelo cliente ou pelo sistema. */
	CANCELADA("Cancelada"),
	/** Período de teste/assinatura expirou. */
	EXPIRADA("Expirada");

	private final String label;

	StatusAssinatura(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	/** Status que, por si só, permitem usar o sistema (sujeito ainda à data de vencimento). */
	public boolean liberaAcesso() {
		return this == TRIAL || this == ATIVA || this == INADIMPLENTE;
	}
}
