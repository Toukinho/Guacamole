package com.guacamole.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.guacamole.backend.entity.RevokedToken;
import java.time.Instant;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {
	@Modifying
	@Query("delete from RevokedToken r where r.expiresAt < :cutoff")
	int deleteExpired(@Param("cutoff") Instant cutoff);
}
