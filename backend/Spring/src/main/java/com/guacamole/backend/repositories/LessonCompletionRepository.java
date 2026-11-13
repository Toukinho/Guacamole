package com.guacamole.backend.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import com.guacamole.backend.entity.LessonCompletion;
import com.guacamole.backend.entity.LessonCompletionId;
/** Acesso as conclusoes, usando a chave composta de usuario e aula. */
public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, LessonCompletionId> {
	/** Conta aulas concluidas pelo usuario no curso informado. */
	long countByUserIdAndLessonCourseId(Long userId, Long courseId);
}
