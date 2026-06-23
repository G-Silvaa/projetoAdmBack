package com.liv.domain;

/** Estados de uma cobrança PIX de assinatura. */
public enum StatusCobranca {

	/** Cobrança criada, aguardando pagamento. */
	PENDENTE("Aguardando pagamento"),
	/** Pagamento confirmado. */
	PAGO("Pago"),
	/** QR code expirou sem pagamento. */
	EXPIRADO("Expirado"),
	/** Cobrança cancelada. */
	CANCELADO("Cancelado");

	private final String label;

	StatusCobranca(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
