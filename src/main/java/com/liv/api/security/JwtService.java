package com.liv.api.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.liv.domain.NivelUsuario;
import com.liv.domain.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	@Value("${app.auth.jwt-secret}")
	private String jwtSecret;

	@Value("${app.auth.jwt-expiration-ms}")
	private long jwtExpirationMs;

	public String generateToken(Usuario usuario) {
		Date now = new Date();

		return Jwts.builder()
				.subject(usuario.getEmail())
				.claim("uid", usuario.getId())
				.claim("nivel", usuario.getNivel().name())
				.issuedAt(now)
				.expiration(new Date(now.getTime() + jwtExpirationMs))
				.signWith(getSecretKey())
				.compact();
	}

	public JwtPayload parseToken(String token) throws JwtException {
		Claims claims = Jwts.parser()
				.verifyWith(getSecretKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();

		Long userId = Long.parseLong(String.valueOf(claims.get("uid")));
		NivelUsuario nivel = NivelUsuario.valueOf(claims.get("nivel", String.class));

		return new JwtPayload(userId, claims.getSubject(), nivel);
	}

	private SecretKey getSecretKey() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
	}

	public record JwtPayload(Long userId, String email, NivelUsuario nivel) {
	}
}
