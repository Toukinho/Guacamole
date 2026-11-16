package com.guacamole.backend.services;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.guacamole.backend.entity.RevokedToken;
import com.guacamole.backend.repositories.RevokedTokenRepository;

@Service
public class TokenService {
	private final SecretKey key;
	private final long expirationSeconds;
	private final RevokedTokenRepository revokedTokens;

	public TokenService(@Value("${security.jwt.secret}") String secret,
			@Value("${security.jwt.expiration-seconds:43200}") long expirationSeconds, RevokedTokenRepository revokedTokens) {
		this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		if (expirationSeconds <= 0) throw new IllegalArgumentException("JWT expiration must be positive");
		this.expirationSeconds = expirationSeconds;
		this.revokedTokens = revokedTokens;
	}

	public String issue(Long userId) {
		Instant now = Instant.now();
		return Jwts.builder().subject(userId.toString()).id(UUID.randomUUID().toString())
				.issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(expirationSeconds))).signWith(key).compact();
	}

	public Long require(String authorization) {
		Claims claims = claims(authorization);
		try {
			if (claims.getId() == null || claims.getExpiration() == null || revokedTokens.existsById(claims.getId())) throw unauthorized();
			long id = Long.parseLong(claims.getSubject());
			if (id <= 0) throw unauthorized();
			return id;
		} catch (NumberFormatException | NullPointerException ex) {
			throw unauthorized();
		}
	}

	@Transactional
	public void revoke(String authorization) {
		require(authorization);
		Claims claims = claims(authorization);
		revokedTokens.save(new RevokedToken(claims.getId(), claims.getExpiration().toInstant()));
	}

	private Claims claims(String authorization) {
		if (authorization == null || !authorization.startsWith("Bearer ")) throw unauthorized();
		try {
			return Jwts.parser().verifyWith(key).build().parseSignedClaims(authorization.substring(7)).getPayload();
		} catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
			throw unauthorized();
		}
	}

	private ResponseStatusException unauthorized() {
		return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
	}
}
