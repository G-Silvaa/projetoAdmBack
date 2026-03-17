package com.liv.api.security;

import java.io.IOException;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import com.liv.domain.Usuario;
import com.liv.domain.UsuarioService;

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
			"/auth/register"
	);

	private static final List<String> PUBLIC_PREFIXES = List.of(
			"/swagger-ui",
			"/api-docs"
	);

	private static final List<String> ADMIN_ONLY_PREFIXES = List.of(
			"/usuarios",
			"/domain/usuario"
	);

	private final JwtService jwtService;
	private final UsuarioService usuarioService;

	public AuthFilter(JwtService jwtService, UsuarioService usuarioService) {
		this.jwtService = jwtService;
		this.usuarioService = usuarioService;
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

			if (isAdminOnly(path) && !authenticatedUser.isAdmin()) {
				writeError(response, HttpStatus.FORBIDDEN, "Acesso permitido apenas para administradores.");
				return;
			}

			request.setAttribute(AuthContext.REQUEST_ATTRIBUTE, authenticatedUser);
			filterChain.doFilter(request, response);
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

	private boolean isAdminOnly(String path) {
		return ADMIN_ONLY_PREFIXES.stream().anyMatch(path::startsWith);
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
