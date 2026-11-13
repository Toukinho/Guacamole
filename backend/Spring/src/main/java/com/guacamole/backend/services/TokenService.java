package com.guacamole.backend.services;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Sessoes locais: sao revogaveis e deixam de existir ao reiniciar a aplicacao. */
@Service
public class TokenService {
	private record Session(Long userId, Instant expiresAt) { }
	private final SecureRandom random = new SecureRandom();
	private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

	public String issue(Long userId) {
		sessions.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(Instant.now()));
		byte[] bytes = new byte[32];
		random.nextBytes(bytes);
		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		sessions.put(token, new Session(userId, Instant.now().plus(Duration.ofHours(12))));
		return token;
	}

	public Long require(String authorization) {
		if (authorization == null || !authorization.startsWith("Bearer ")) throw unauthorized();
		String token = authorization.substring(7);
		Session session = sessions.get(token);
		if (session == null) throw unauthorized();
		if (!session.expiresAt().isAfter(Instant.now())) {
			sessions.remove(token, session);
			throw unauthorized();
		}
		return session.userId();
	}

	public Long optional(String authorization) {
		return authorization == null ? null : require(authorization);
	}

	public void revoke(String authorization) {
		require(authorization);
		sessions.remove(authorization.substring(7));
	}

	private ResponseStatusException unauthorized() {
		return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
	}
}
