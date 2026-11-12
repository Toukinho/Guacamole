package com.guacamole.backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.guacamole.backend.entity.Course;

/** Acesso aos cursos; a consulta paginada carrega tambem o professor. */
public interface CourseRepository extends JpaRepository<Course, Long> {
	/** Busca uma pagina de cursos com seus professores para evitar consultas adicionais durante o mapeamento. */
	@EntityGraph(attributePaths = "teacher")
	Page<Course> findAll(Pageable pageable);
}
