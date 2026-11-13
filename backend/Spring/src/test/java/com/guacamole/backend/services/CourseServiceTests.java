package com.guacamole.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.guacamole.backend.entity.Course;
import com.guacamole.backend.entity.Teacher;
import com.guacamole.backend.repositories.CourseRepository;

class CourseServiceTests {
	@Test
	void appliesStableSortingAndMapsTeacher() {
		CourseRepository repository = mock(CourseRepository.class);
		Teacher teacher = new Teacher("Ana");
		when(repository.findAll(any(Pageable.class))).thenAnswer(invocation -> new PageImpl<>(List.of(new Course(teacher, "Java", null, null)), invocation.getArgument(0), 1));
		var result = new CourseService(repository).list(0, 20);
		var pageable = ArgumentCaptor.forClass(Pageable.class);
		verify(repository).findAll(pageable.capture());
		assertEquals("title: ASC,id: ASC", pageable.getValue().getSort().toString());
		assertEquals("Ana", result.content().get(0).teacher());
		assertEquals(1, result.totalElements());
	}
	@Test
	void rejectsInvalidPagination() {
		CourseService service = new CourseService(mock(CourseRepository.class));
		for (int[] values : new int[][] {{-1, 20}, {0, 0}, {0, 51}}) {
			ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.list(values[0], values[1]));
			assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
		}
	}
}
