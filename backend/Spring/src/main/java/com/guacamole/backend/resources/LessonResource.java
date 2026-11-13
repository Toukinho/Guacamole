package com.guacamole.backend.resources;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.guacamole.backend.dto.LessonDto;
import com.guacamole.backend.services.CourseQueryService;
import com.guacamole.backend.services.TokenService;
import com.guacamole.backend.services.UserCourseService;

@RestController
@RequestMapping("/lessons")
public class LessonResource {
	private final CourseQueryService query;
	private final UserCourseService personal;
	private final TokenService tokens;
	public LessonResource(CourseQueryService query, UserCourseService personal, TokenService tokens) {
		this.query = query; this.personal = personal; this.tokens = tokens;
	}
	@GetMapping("/{id}")
	public ResponseEntity<LessonDto> detail(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorization) {
		return ResponseEntity.ok(query.lesson(id, tokens.optional(authorization)));
	}
	@PostMapping("/{id}/complete")
	public ResponseEntity<Void> complete(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorization) {
		personal.completeLesson(tokens.require(authorization), id);
		return ResponseEntity.noContent().build();
	}
	@DeleteMapping("/{id}/complete")
	public ResponseEntity<Void> remove(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorization) {
		personal.removeCompletion(tokens.require(authorization), id);
		return ResponseEntity.noContent().build();
	}
}
