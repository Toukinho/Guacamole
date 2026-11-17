package com.guacamole.backend.resources;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.guacamole.backend.dto.LessonDto;
import com.guacamole.backend.services.CourseQueryService;
import com.guacamole.backend.services.UserCourseService;

@RestController
@RequestMapping("/lessons")
public class LessonResource {
	private final CourseQueryService query;
	private final UserCourseService personal;
	public LessonResource(CourseQueryService query, UserCourseService personal) {
		this.query = query; this.personal = personal;
	}
	@GetMapping("/{id}")
	public ResponseEntity<LessonDto> detail(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
		return ResponseEntity.ok(query.lesson(id, userId));
	}
	@PostMapping("/{id}/complete")
	public ResponseEntity<Void> complete(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
		personal.completeLesson(userId, id);
		return ResponseEntity.noContent().build();
	}
	@DeleteMapping("/{id}/complete")
	public ResponseEntity<Void> remove(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
		personal.removeCompletion(userId, id);
		return ResponseEntity.noContent().build();
	}
}
