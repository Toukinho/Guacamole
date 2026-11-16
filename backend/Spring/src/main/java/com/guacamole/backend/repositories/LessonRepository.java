package com.guacamole.backend.repositories;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.guacamole.backend.entity.Lesson;
/** Consultas de aulas por curso, incluindo ordenacao e contagem. */
public interface LessonRepository extends JpaRepository<Lesson, Long> {
	@org.springframework.data.jpa.repository.Query("select l.course.id, count(l) from Lesson l where exists (select w from CourseWatchlist w where w.user.id = :userId and w.course = l.course) group by l.course.id")
	java.util.List<Object[]> countForWatchlist(@org.springframework.data.repository.query.Param("userId") Long userId);
	/** Lista aulas do curso em ordem crescente de posicao. */
	List<Lesson> findByCourseIdOrderByPositionAsc(Long courseId);
	/** Conta as aulas existentes no curso. */
	long countByCourseId(Long courseId);
}
