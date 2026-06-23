package com.liv.api.security;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import com.liv.domain.SubscriptionService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Impõe o limite de clientes do plano na criação de clientes (endpoint gerado
 * pelo framework: POST /domain/cliente). Roda depois do {@link AuthFilter},
 * com o tenant já definido — então o COUNT de clientes já vem isolado por
 * empresa via @TenantId.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 30)
public class PlanLimitFilter extends OncePerRequestFilter {

	private final SubscriptionService subscriptionService;

	@PersistenceContext
	private EntityManager entityManager;

	public PlanLimitFilter(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		if (isCriacaoDeCliente(request)) {
			Object attr = request.getAttribute(AuthContext.REQUEST_ATTRIBUTE);
			if (attr instanceof AuthenticatedUser user) {
				try {
					long atual = entityManager
							.createQuery("SELECT COUNT(c) FROM Cliente c", Long.class)
							.getSingleResult();
					subscriptionService.assertDentroDoLimiteDeClientes(user.empresaId(), atual);
				} catch (ResponseStatusException exception) {
					writeError(response, HttpStatus.valueOf(exception.getStatusCode().value()), exception.getReason());
					return;
				}
			}
		}

		filterChain.doFilter(request, response);
	}

	private boolean isCriacaoDeCliente(HttpServletRequest request) {
		if (!"POST".equalsIgnoreCase(request.getMethod())) {
			return false;
		}
		return getPath(request).startsWith("/domain/cliente");
	}

	private String getPath(HttpServletRequest request) {
		String path = request.getRequestURI();
		String contextPath = request.getContextPath();

		if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
			return path.substring(contextPath.length());
		}

		return path;
	}

	private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"message\":\"" + escape(message) + "\"}");
	}

	private String escape(String value) {
		return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
