package com.liv.api.dto;

import java.math.BigDecimal;
import java.util.Date;

/** Cobrança PIX devolvida ao front para exibir o QR code e acompanhar o status. */
public record CobrancaResponseDTO(
		Long id,
		String planoNome,
		BigDecimal valor,
		String status,
		String statusLabel,
		String brCode,
		String brCodeBase64,
		Date expiraEm,
		boolean pago
) {
}
