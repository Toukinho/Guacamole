package com.guacamole.backend.repositories;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.guacamole.backend.entity.AuthToken;

public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {
	@Query("select t from AuthToken t join fetch t.user where t.tokenHash = :hash")
	Optional<AuthToken> findWithUser(@Param("hash") String hash);
	@Modifying
	@Query("delete from AuthToken t where t.tokenHash = :hash and t.expiresAt > :now")
	int deleteValid(@Param("hash") String hash, @Param("now") Instant now);
	@Modifying
	@Query("delete from AuthToken t where t.expiresAt <= :now")
	int deleteExpired(@Param("now") Instant now);
}
