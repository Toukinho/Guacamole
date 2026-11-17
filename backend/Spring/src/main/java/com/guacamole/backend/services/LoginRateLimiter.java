package com.guacamole.backend.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Limite local por instancia; nao confia em cabecalhos fornecidos pelo cliente. */
@Component
public class LoginRateLimiter {
	private static final Duration WINDOW = Duration.ofMinutes(15);
	private static final int MAX_KEYS = 100_000;
	private final Map<String, Window> windows = new HashMap<>();
	private final Clock clock;
	private record Window(Instant until, int count) { }

	public LoginRateLimiter() { this(Clock.systemUTC()); }
	LoginRateLimiter(Clock clock) { this.clock = clock; }

	public synchronized void check(String ip, String email) {
		check("login", ip, email);
	}

	public synchronized void checkRegistration(String ip, String email) {
		check("register", ip, email);
	}

	private void check(String scope, String ip, String email) {
		Instant now = clock.instant();
		windows.entrySet().removeIf(entry -> !entry.getValue().until().isAfter(now));
		String ipKey = scope + ":ip:" + digest(ip);
		String accountKey = scope + ":account:" + digest(email.trim().toLowerCase(Locale.ROOT));
		if (windows.size() > MAX_KEYS - 2 || count(ipKey) >= 30 || count(accountKey) >= 5)
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many attempts; try again later");
		increment(ipKey, now);
		increment(accountKey, now);
	}

	private int count(String key) { return windows.containsKey(key) ? windows.get(key).count() : 0; }
	private void increment(String key, Instant now) {
		Window current = windows.get(key);
		windows.put(key, current == null ? new Window(now.plus(WINDOW), 1) : new Window(current.until(), current.count() + 1));
	}
	private String digest(String value) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException(ex);
		}
	}
}
