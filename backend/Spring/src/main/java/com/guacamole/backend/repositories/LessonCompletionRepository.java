package com.guacamole.backend.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import com.guacamole.backend.entity.LessonCompletion;
import com.guacamole.backend.entity.LessonCompletionId;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
/** Acesso as conclusoes, usando a chave composta de usuario e aula. */
public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, LessonCompletionId> {
	/** Conta aulas concluidas pelo usuario no curso informado. */
	long countByUserIdAndLessonCourseId(Long userId, Long courseId);
	@Query("select c.lesson.id from LessonCompletion c where c.user.id = :userId and c.lesson.course.id = :courseId")
	List<Long> findCompletedLessonIds(@Param("userId") Long userId, @Param("courseId") Long courseId);
	@Query("select c.lesson.course.id, count(c) from LessonCompletion c where c.user.id = :userId and exists (select w from CourseWatchlist w where w.user.id = :userId and w.course = c.lesson.course) group by c.lesson.course.id")
	List<Object[]> countCompletedForWatchlist(@Param("userId") Long userId);
}
