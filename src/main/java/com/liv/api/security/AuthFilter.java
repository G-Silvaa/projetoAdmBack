package com.liv.api.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import com.liv.domain.NivelUsuario;
import com.liv.domain.SubscriptionService;
import com.liv.domain.Usuario;
import com.liv.domain.UsuarioService;
import com.liv.infra.tenant.TenantContext;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class AuthFilter extends OncePerRequestFilter {

	private static final List<String> PUBLIC_PATHS = List.of(
			"/",
			"/error",
			"/auth/login",
			"/auth/register",
			"/planos"
	);

	private static final List<String> PUBLIC_PREFIXES = List.of(
			"/swagger-ui",
			"/api-docs"
	);

	// Endpoints liberados mesmo com a assinatura bloqueada, para o front
	// conseguir carregar o usuário e exibir a tela de cobrança.
	private static final List<String> SUBSCRIPTION_BYPASS = List.of(
			"/auth/me",
			"/assinatura"
	);

	private static final List<AccessRule> ACCESS_RULES = List.of(
			new AccessRule(
					List.of("/usuarios", "/domain/usuario"),
					List.of(),
					List.of(),
					List.of(NivelUsuario.ADMINISTRADOR)
			),
			new AccessRule(
					List.of("/dashboard", "/processo", "/domain/relatorio", "/domain/service/relatorio-service"),
					List.of(),
					List.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PATCH),
					List.of(
							NivelUsuario.ADMINISTRADOR,
							NivelUsuario.GESTOR,
							NivelUsuario.OPERADOR,
							NivelUsuario.FINANCEIRO,
							NivelUsuario.CONSULTA
					)
			),
			new AccessRule(
					List.of("/domain/cliente"),
					List.of(),
					List.of(HttpMethod.GET),
					List.of(
							NivelUsuario.ADMINISTRADOR,
							NivelUsuario.GESTOR,
							NivelUsuario.OPERADOR,
							NivelUsuario.CONSULTA
					)
			),
			new AccessRule(
					List.of("/domain/cliente"),
					List.of(),
					List.of(HttpMethod.POST, HttpMethod.PATCH, HttpMethod.DELETE),
					List.of(
							NivelUsuario.ADMINISTRADOR,
							NivelUsuario.GESTOR,
							NivelUsuario.OPERADOR
					)
			),
			new AccessRule(
					List.of("/domain/processo"),
					List.of(),
					List.of(HttpMethod.GET),
					List.of(
							NivelUsuario.ADMINISTRADOR,
							NivelUsuario.GESTOR,
							NivelUsuario.OPERADOR,
							NivelUsuario.CONSULTA
					)
			),
			new AccessRule(
					List.of("/domain/processo"),
					List.of(),
					List.of(HttpMethod.POST, HttpMethod.PATCH, HttpMethod.DELETE),
					List.of(
							NivelUsuario.ADMINISTRADOR,
							NivelUsuario.GESTOR,
							NivelUsuario.OPERADOR
					)
			),
			new AccessRule(
					List.of("/domain/contrato/"),
					List.of("/gerar-contrato"),
					List.of(HttpMethod.PATCH),
					List.of(
							NivelUsuario.ADMINISTRADOR,
							NivelUsuario.GESTOR,
							NivelUsuario.OPERADOR,
							NivelUsuario.FINANCEIRO,
							NivelUsuario.CONSULTA
					)
			),
			new AccessRule(
					List.of("/domain/contrato"),
					List.of(),
					List.of(HttpMethod.GET),
					List.of(
							NivelUsuario.ADMINISTRADOR,
							NivelUsuario.GESTOR,
							NivelUsuario.OPERADOR,
							NivelUsuario.FINANCEIRO,
							NivelUsuario.CONSULTA
					)
			),
			new AccessRule(
					List.of("/domain/contrato/"),
					List.of("/renovar"),
					List.of(HttpMethod.PATCH),
					List.of(
							NivelUsuario.ADMINISTRADOR,
							NivelUsuario.GESTOR,
							NivelUsuario.OPERADOR
					)
			),
			new AccessRule(
					List.of("/domain/contrato"),
					List.of(),
					List.of(HttpMethod.POST, HttpMethod.DELETE),
					List.of(
							NivelUsuario.ADMINISTRADOR,
							NivelUsuario.GESTOR,
							NivelUsuario.OPERADOR
					)
			),
			new AccessRule(
					List.of("/domain/contrato"),
					List.of(),
					List.of(HttpMethod.PATCH),
					List.of(
							NivelUsuario.ADMINISTRADOR,
							NivelUsuario.GESTOR,
							NivelUsuario.OPERADOR
					)
			),
			new AccessRule(
					List.of("/domain/financeiro"),
					List.of(),
					List.of(HttpMethod.GET),
					List.of(
							NivelUsuario.ADMINISTRADOR,
							NivelUsuario.GESTOR,
							NivelUsuario.FINANCEIRO,
							NivelUsuario.CONSULTA
					)
			),
			new AccessRule(
					List.of("/domain/financeiro/"),
					List.of("/boleto", "/comprovante"),
					List.of(HttpMethod.PATCH),
					List.of(
							NivelUsuario.ADMINISTRADOR,
							NivelUsuario.GESTOR,
							NivelUsuario.FINANCEIRO
					)
			),
			new AccessRule(
					List.of("/domain/financeiro"),
					List.of(),
					List.of(HttpMethod.PATCH),
					List.of(
							NivelUsuario.ADMINISTRADOR,
							NivelUsuario.GESTOR,
							NivelUsuario.FINANCEIRO
					)
			)
	);

	private final JwtService jwtService;
	private final UsuarioService usuarioService;
	private final SubscriptionService subscriptionService;

	public AuthFilter(JwtService jwtService, UsuarioService usuarioService, SubscriptionService subscriptionService) {
		this.jwtService = jwtService;
		this.usuarioService = usuarioService;
		this.subscriptionService = subscriptionService;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String path = getPath(request);

		if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isPublic(path)) {
			filterChain.doFilter(request, response);
			return;
		}

		String authorization = request.getHeader("Authorization");

		if (authorization == null || !authorization.startsWith("Bearer ")) {
			writeError(response, HttpStatus.UNAUTHORIZED, "Faça login para continuar.");
			return;
		}

		String token = authorization.substring(7).trim();

		try {
			JwtService.JwtPayload payload = jwtService.parseToken(token);
			Usuario usuario = usuarioService.getUsuarioAtivo(payload.userId());
			AuthenticatedUser authenticatedUser = AuthenticatedUser.fromEntity(usuario);

			if (!isAuthorized(path, request.getMethod(), authenticatedUser)) {
				writeError(response, HttpStatus.FORBIDDEN, "Seu perfil não tem permissão para esta operação.");
				return;
			}

			request.setAttribute(AuthContext.REQUEST_ATTRIBUTE, authenticatedUser);
			TenantContext.set(usuario.getEmpresaId());
			try {
				// Gate de assinatura: bloqueia o sistema quando a assinatura está
				// vencida/inativa (exceto endpoints liberados para exibir a cobrança).
				if (!isSubscriptionBypass(path) && !subscriptionService.permiteAcesso(usuario.getEmpresaId())) {
					writeError(response, HttpStatus.PAYMENT_REQUIRED,
							"Sua assinatura está inativa ou vencida. Regularize o pagamento para continuar.");
					return;
				}
				filterChain.doFilter(request, response);
			} finally {
				TenantContext.clear();
			}
		} catch (JwtException exception) {
			writeError(response, HttpStatus.UNAUTHORIZED, "Sessão inválida ou expirada.");
		} catch (ResponseStatusException exception) {
			writeError(response, HttpStatus.valueOf(exception.getStatusCode().value()), exception.getReason());
		} catch (Exception exception) {
			writeError(response, HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível validar a sessão.");
		}
	}

	private boolean isPublic(String path) {
		return PUBLIC_PATHS.contains(path) || PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
	}

	private boolean isSubscriptionBypass(String path) {
		return SUBSCRIPTION_BYPASS.stream().anyMatch(path::startsWith);
	}

	private boolean isAuthorized(String path, String method, AuthenticatedUser authenticatedUser) {
		Optional<AccessRule> rule = ACCESS_RULES.stream()
				.filter(accessRule -> accessRule.matches(path, method))
				.findFirst();

		return rule.map(accessRule -> accessRule.allows(authenticatedUser.nivel())).orElse(true);
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
