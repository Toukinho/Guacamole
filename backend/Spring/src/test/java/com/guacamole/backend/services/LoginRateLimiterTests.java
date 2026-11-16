package com.guacamole.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class LoginRateLimiterTests {
	@Test
	void limitsAccountAcrossIpsAndIpAcrossAccounts() {
		LoginRateLimiter limiter = new LoginRateLimiter(Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));
		for (int i = 0; i < 5; i++) limiter.check("ip" + i, "USER@example.com");
		assertEquals(HttpStatus.TOO_MANY_REQUESTS, assertThrows(ResponseStatusException.class,
				() -> limiter.check("new-ip", "user@example.com")).getStatusCode());
		for (int i = 0; i < 30; i++) limiter.check("shared-ip", i + "@example.com");
		assertEquals(HttpStatus.TOO_MANY_REQUESTS, assertThrows(ResponseStatusException.class,
				() -> limiter.check("shared-ip", "another@example.com")).getStatusCode());
	}
}
