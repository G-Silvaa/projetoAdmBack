package com.liv.api.dto;

import java.math.BigDecimal;

import com.liv.domain.Plano;

public record PlanoResponseDTO(
		String codigo,
		String nome,
		BigDecimal preco,
		Integer maxUsuarios,
		Integer maxClientes
) {

	public static PlanoResponseDTO fromEntity(Plano plano) {
		return new PlanoResponseDTO(
				plano.getCodigo(),
				plano.getNome(),
				plano.getPreco(),
				plano.getMaxUsuarios(),
				plano.getMaxClientes()
		);
	}
}
