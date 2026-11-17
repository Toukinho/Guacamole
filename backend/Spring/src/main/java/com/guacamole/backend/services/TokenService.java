package com.guacamole.backend.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.guacamole.backend.entity.AuthToken;
import com.guacamole.backend.repositories.AuthTokenRepository;
import com.guacamole.backend.repositories.UserRepository;

@Service
public class TokenService {
	private final SecureRandom random = new SecureRandom();
	private final long expirationSeconds;
	private final AuthTokenRepository tokens;
	private final UserRepository users;

	public TokenService(@Value("${security.token.expiration-seconds:43200}") long expirationSeconds,
			AuthTokenRepository tokens, UserRepository users) {
		if (expirationSeconds <= 0) throw new IllegalArgumentException("Token expiration must be positive");
		this.expirationSeconds = expirationSeconds;
		this.tokens = tokens;
		this.users = users;
	}

	@Transactional
	public String issue(Long userId) {
		byte[] bytes = new byte[32];
		random.nextBytes(bytes);
		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		Instant now = Instant.now();
		tokens.save(new AuthToken(hash(token), users.getReferenceById(userId), now, now.plusSeconds(expirationSeconds)));
		return token;
	}

	@Transactional(readOnly = true)
	public Long require(String authorization) {
		AuthToken token = tokens.findWithUser(hash(bearer(authorization))).orElseThrow(this::unauthorized);
		if (!token.getExpiresAt().isAfter(Instant.now())) throw unauthorized();
		return token.getUserId();
	}

	@Transactional
	public void revoke(String authorization) {
		if (tokens.deleteValid(hash(bearer(authorization)), Instant.now()) != 1) throw unauthorized();
	}

	@Scheduled(fixedDelayString = "${security.token.cleanup-interval-ms:3600000}", initialDelayString = "${security.token.cleanup-interval-ms:3600000}")
	@Transactional
	public void cleanExpired() { tokens.deleteExpired(Instant.now()); }

	private String bearer(String authorization) {
		if (authorization == null || !authorization.startsWith("Bearer ")) throw unauthorized();
		String token = authorization.substring(7);
		if (!token.matches("[A-Za-z0-9_-]{43}")) throw unauthorized();
		return token;
	}

	private String hash(String token) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.US_ASCII)));
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 unavailable", ex);
		}
	}

	private ResponseStatusException unauthorized() {
		return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
	}
}
