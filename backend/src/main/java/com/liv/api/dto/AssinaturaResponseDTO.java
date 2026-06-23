package com.liv.api.dto;

import java.math.BigDecimal;
import java.util.Date;

public record AssinaturaResponseDTO(
		String planoCodigo,
		String planoNome,
		BigDecimal preco,
		String status,
		String statusLabel,
		Date trialAte,
		Date vencimento,
		boolean permiteAcesso,
		Integer diasRestantes,
		Integer maxUsuarios,
		Integer maxClientes
) {
}
