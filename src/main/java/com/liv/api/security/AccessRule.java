package com.liv.api.security;

import java.util.List;

import org.springframework.http.HttpMethod;

import com.liv.domain.NivelUsuario;

public record AccessRule(
		List<String> pathPrefixes,
		List<String> pathSuffixes,
		List<HttpMethod> methods,
		List<NivelUsuario> allowedRoles
) {

	public boolean matches(String path, String method) {
		boolean pathMatches = pathPrefixes.stream().anyMatch(path::startsWith);
		boolean suffixMatches = pathSuffixes.isEmpty() || pathSuffixes.stream().anyMatch(path::endsWith);
		boolean methodMatches = methods.isEmpty() || methods.stream().anyMatch(httpMethod -> httpMethod.matches(method));

		return pathMatches && suffixMatches && methodMatches;
	}

	public boolean allows(NivelUsuario nivelUsuario) {
		return allowedRoles.contains(nivelUsuario);
	}
}
