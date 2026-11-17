package com.guacamole.backend.services;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.guacamole.backend.entity.Lesson;
import com.guacamole.backend.repositories.CourseRepository;
import com.guacamole.backend.repositories.LessonRepository;

/** Consulta aulas e valida a existencia do curso solicitado. */
@Service
public class LessonService {
	private final LessonRepository lessons;
	private final CourseRepository courses;
	public LessonService(LessonRepository lessons, CourseRepository courses) { this.lessons = lessons; this.courses = courses; }
	/** Busca uma aula pelo ID; retorna erro 404 quando nao existe. */
	@Transactional(readOnly = true)
	public Lesson findById(Long id) {
		return lessons.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
	}
	/** Lista as aulas de um curso em ordem de posicao; retorna erro 404 se o curso nao existir. */
	@Transactional(readOnly = true)
	public List<Lesson> findByCourse(Long courseId) {
		if (!courses.existsById(courseId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
		return lessons.findByCourseIdOrderByPositionAsc(courseId);
	}
}
