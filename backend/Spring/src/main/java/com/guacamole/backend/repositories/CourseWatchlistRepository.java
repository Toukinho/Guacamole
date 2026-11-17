package com.guacamole.backend.repositories;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.guacamole.backend.entity.CourseWatchlist;
import com.guacamole.backend.entity.CourseWatchlistId;
/** Acesso aos cursos salvos, usando a chave composta de usuario e curso. */
public interface CourseWatchlistRepository extends JpaRepository<CourseWatchlist, CourseWatchlistId> {
	/** Lista vinculos do usuario carregando curso e professor. */
	@EntityGraph(attributePaths = {"course", "course.teacher"})
	List<CourseWatchlist> findByUserId(Long userId);
}
