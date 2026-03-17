package com.liv.api.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

public final class AuthContext {

	public static final String REQUEST_ATTRIBUTE = "authenticatedUser";

	private AuthContext() {
	}

	public static AuthenticatedUser requireUser(HttpServletRequest request) {
		Object authenticatedUser = request.getAttribute(REQUEST_ATTRIBUTE);

		if (authenticatedUser instanceof AuthenticatedUser user) {
			return user;
		}

		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado.");
	}

	public static AuthenticatedUser requireAdmin(HttpServletRequest request) {
		AuthenticatedUser user = requireUser(request);

		if (!user.isAdmin()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso permitido apenas para administradores.");
		}

		return user;
	}
}
