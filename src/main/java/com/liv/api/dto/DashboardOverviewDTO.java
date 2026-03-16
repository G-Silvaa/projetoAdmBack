package com.liv.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardOverviewDTO(
		Long totalClientes,
		Long contratosAtivos,
		Long contratosEncerrados,
		Long processosEmAndamento,
		Long processosPendentes,
		Long processosConcedidos,
		Long financeirosEmAberto,
		Long financeirosQuitados,
		BigDecimal valorCarteira,
		List<DashboardStatusDTO> statusProcessos) {
}
