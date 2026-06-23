package com.liv.infra.abacatepay;

import java.util.Date;

/**
 * Resultado de uma cobrança PIX no provedor (normalizado para uso interno).
 *
 * @param providerId   id da cobrança no AbacatePay
 * @param status       status do provedor (PENDING, PAID, EXPIRED, CANCELLED...)
 * @param brCode       PIX copia e cola (payload EMV)
 * @param brCodeBase64 imagem do QR code em data URI
 * @param expiresAt    expiração do QR code (pode ser nulo)
 */
public record PixCharge(
		String providerId,
		String status,
		String brCode,
		String brCodeBase64,
		Date expiresAt
) {

	public boolean isPaid() {
		return "PAID".equalsIgnoreCase(status);
	}
}
