package com.liv.api.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liv.api.dto.DashboardOverviewDTO;
import com.liv.api.dto.DashboardStatusDTO;
import com.liv.domain.Status;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

	@PersistenceContext
	private EntityManager entityManager;

	@GetMapping("/overview")
	public ResponseEntity<DashboardOverviewDTO> getOverview() {
		Long totalClientes = queryCount("SELECT COUNT(c) FROM Cliente c");
		Long contratosAtivos = queryCount("SELECT COUNT(c) FROM Contrato c WHERE c.conclusao IS NULL");
		Long contratosEncerrados = queryCount("SELECT COUNT(c) FROM Contrato c WHERE c.conclusao IS NOT NULL");
		Long processosEmAndamento = queryProgressCount(Status.APROVADO, Status.REPROVADO);
		Long processosPendentes = queryPendingCount(Status.AGUARDANDO, Status.PENDENTE);
		Long processosConcedidos = queryStatusCount(Status.APROVADO);
		Long financeirosEmAberto = queryCount(
				"SELECT COUNT(f) FROM Financeiro f WHERE COALESCE(f.situacaoPagamento, false) = false");
		Long financeirosQuitados = queryCount(
				"SELECT COUNT(f) FROM Financeiro f WHERE COALESCE(f.situacaoPagamento, false) = true");
		BigDecimal valorCarteira = entityManager
				.createQuery("SELECT COALESCE(SUM(c.valor), 0) FROM Contrato c", BigDecimal.class)
				.getSingleResult();

		List<DashboardStatusDTO> statusProcessos = entityManager
				.createQuery("SELECT p.status, COUNT(p) FROM Processo p GROUP BY p.status ORDER BY COUNT(p) DESC",
						Object[].class)
				.getResultList()
				.stream()
				.map(result -> new DashboardStatusDTO(result[0].toString(), ((Number) result[1]).longValue()))
				.toList();

		return ResponseEntity.ok(new DashboardOverviewDTO(
				totalClientes,
				contratosAtivos,
				contratosEncerrados,
				processosEmAndamento,
				processosPendentes,
				processosConcedidos,
				financeirosEmAberto,
				financeirosQuitados,
				valorCarteira,
				statusProcessos));
	}

	private Long queryCount(String jpql) {
		return entityManager.createQuery(jpql, Long.class).getSingleResult();
	}

	private Long queryStatusCount(Status status) {
		return entityManager.createQuery("SELECT COUNT(p) FROM Processo p WHERE p.status = :status", Long.class)
				.setParameter("status", status)
				.getSingleResult();
	}

	private Long queryProgressCount(Status approvedStatus, Status rejectedStatus) {
		return entityManager
				.createQuery("SELECT COUNT(p) FROM Processo p WHERE p.status <> :aprovado AND p.status <> :reprovado",
						Long.class)
				.setParameter("aprovado", approvedStatus)
				.setParameter("reprovado", rejectedStatus)
				.getSingleResult();
	}

	private Long queryPendingCount(Status waitingStatus, Status pendingStatus) {
		return entityManager
				.createQuery("SELECT COUNT(p) FROM Processo p WHERE p.status = :aguardando OR p.status = :pendente",
						Long.class)
				.setParameter("aguardando", waitingStatus)
				.setParameter("pendente", pendingStatus)
				.getSingleResult();
	}
}
