package com.guacamole.backend.services;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

/** Armazena apenas hash com salt e fator de trabalho, nunca a senha recebida. */
@Component
public class PasswordService {
	private static final int ITERATIONS = 600_000;
	private final SecureRandom random = new SecureRandom();

	public String hash(String password) {
		byte[] salt = new byte[16];
		random.nextBytes(salt);
		return "pbkdf2-sha256$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$"
				+ Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
	}

	public boolean matches(String password, String encoded) {
		try {
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
