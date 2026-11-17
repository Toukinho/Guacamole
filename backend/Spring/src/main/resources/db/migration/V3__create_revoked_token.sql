CREATE TABLE revoked_token (
                               id VARCHAR(36) NOT NULL,
                               expires_at DATETIME(6) NOT NULL,
                               PRIMARY KEY (id),
                               INDEX idx_revoked_token_expiration (expires_at)
) ENGINE=InnoDB;