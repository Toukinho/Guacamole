package com.guacamole.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.guacamole.backend.dto.CoursePageDto;
import com.guacamole.backend.dto.CourseSummaryDto;
import com.guacamole.backend.entity.Course;
import com.guacamole.backend.repositories.CourseRepository;

/** Consulta cursos e prepara a pagina de resultados para a API. */
@Service
public class CourseService {
	private final CourseRepository courses;
	public CourseService(CourseRepository courses) { this.courses = courses; }
	/** Retorna cursos ordenados por titulo e ID. Aceita pagina a partir de zero e tamanho de 1 a 50; valores invalidos geram erro 400. */
	@Transactional(readOnly = true)
	public CoursePageDto list(int page, int size) {
		if (page < 0 || size < 1 || size > 50) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page or size");
		Page<Course> result = courses.findAll(PageRequest.of(page, size, Sort.by("title").ascending().and(Sort.by("id").ascending())));
		return new CoursePageDto(result.map(c -> new CourseSummaryDto(c.getId(), c.getTitle(), c.getTeacher().getName())).getContent(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
	}
	/** Busca um curso pelo ID; retorna erro 404 quando nao existe. */
	@Transactional(readOnly = true)
	public Course findById(Long id) {
		return courses.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
	}
}
