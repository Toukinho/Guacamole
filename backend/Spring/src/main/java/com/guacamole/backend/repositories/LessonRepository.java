package com.guacamole.backend.repositories;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.guacamole.backend.entity.Lesson;
/** Consultas de aulas por curso, incluindo ordenacao e contagem. */
public interface LessonRepository extends JpaRepository<Lesson, Long> {
	/** Lista aulas do curso em ordem crescente de posicao. */
	List<Lesson> findByCourseIdOrderByPositionAsc(Long courseId);
	/** Conta as aulas existentes no curso. */
	long countByCourseId(Long courseId);
}
