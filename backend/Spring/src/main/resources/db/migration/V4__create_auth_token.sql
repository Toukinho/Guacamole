CREATE TABLE auth_token (
	token_hash VARCHAR(64) NOT NULL,
	user_id BIGINT NOT NULL,
	created_at DATETIME(6) NOT NULL,
	expires_at DATETIME(6) NOT NULL,
	PRIMARY KEY (token_hash),
	CONSTRAINT fk_auth_token_user FOREIGN KEY (user_id) REFERENCES `user` (id),
	INDEX idx_auth_token_expiration (expires_at)
) ENGINE=InnoDB;
