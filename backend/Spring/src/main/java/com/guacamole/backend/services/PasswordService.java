package com.guacamole.backend.services;

import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Armazena apenas hash com salt e fator de trabalho, nunca a senha recebida. */
@Component
public class PasswordService {
	private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

	public String hash(String password) {
		if (password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
			throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Password exceeds 72 bytes");
		return bcrypt.encode(password);
	}

	public boolean matches(String password, String encoded) {
		try {
			if (encoded.startsWith("$2")) return password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72 && bcrypt.matches(password, encoded);
			String[] parts = encoded.split("\\$");
			if (parts.length != 4 || !parts[0].equals("pbkdf2-sha256")) return false;
			int iterations = Integer.parseInt(parts[1]);
			if (iterations < 1 || iterations > 2_000_000) return false;
			return MessageDigest.isEqual(Base64.getDecoder().decode(parts[3]),
					derive(password, Base64.getDecoder().decode(parts[2]), iterations));
		} catch (IllegalArgumentException | NullPointerException ex) {
			return false;
		}
	}

	private byte[] derive(String password, byte[] salt, int iterations) {
		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
		try {
			return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
		} catch (java.security.GeneralSecurityException ex) {
			throw new IllegalStateException("Password hashing unavailable", ex);
		} finally {
			spec.clearPassword();
		}
	}
}
