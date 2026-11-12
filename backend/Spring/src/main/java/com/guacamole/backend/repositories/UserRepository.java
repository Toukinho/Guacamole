package com.guacamole.backend.repositories;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.guacamole.backend.entity.User;
/** Acesso aos usuarios, incluindo busca por email e bloqueio para operacoes de escrita. */
public interface UserRepository extends JpaRepository<User, Long> {
	/** Busca um usuario pelo email. */
	Optional<User> findByEmail(String email);
	/** Bloqueia a linha do usuario durante a transacao para coordenar alteracoes concorrentes. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from User u where u.id = :id")
	Optional<User> lockById(@Param("id") Long id);
}
