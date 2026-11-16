package com.guacamole.backend.services;

import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.guacamole.backend.repositories.RevokedTokenRepository;

@Component
public class RevokedTokenCleanup {
	private final RevokedTokenRepository tokens;
	public RevokedTokenCleanup(RevokedTokenRepository tokens) { this.tokens = tokens; }

	@Scheduled(fixedDelayString = "${security.jwt.cleanup-interval-ms:3600000}", initialDelayString = "${security.jwt.cleanup-interval-ms:3600000}")
	@Transactional
	public void cleanExpired() { tokens.deleteExpired(Instant.now()); }
}
