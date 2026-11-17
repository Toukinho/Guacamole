package com.guacamole.backend.entity;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
@Table(name = "auth_token")
public class AuthToken {
	@Id
	@Column(name = "token_hash", length = 64)
	private String tokenHash;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	protected AuthToken() { }
	public AuthToken(String tokenHash, User user, Instant createdAt, Instant expiresAt) {
		this.tokenHash = tokenHash;
		this.user = user;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
	}
	public Long getUserId() { return user.getId(); }
	public Instant getExpiresAt() { return expiresAt; }
}
