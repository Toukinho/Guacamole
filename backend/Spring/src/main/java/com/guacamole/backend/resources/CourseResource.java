package com.guacamole.backend.resources;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.util.List;
import com.guacamole.backend.dto.CoursePageDto;
import com.guacamole.backend.dto.CourseDetailDto;
import com.guacamole.backend.dto.LessonDto;
import com.guacamole.backend.dto.ProgressResponseDto;
import com.guacamole.backend.services.CourseService;
import com.guacamole.backend.services.CourseQueryService;
import com.guacamole.backend.services.TokenService;
import com.guacamole.backend.services.UserCourseService;

@RestController
@RequestMapping("/courses")
public class CourseResource {
	private final CourseService courses;
	private final CourseQueryService query;
	private final UserCourseService personal;
	private final TokenService tokens;
	public CourseResource(CourseService courses, CourseQueryService query, UserCourseService personal, TokenService tokens) {
		this.courses = courses; this.query = query; this.personal = personal; this.tokens = tokens;
	}
	@GetMapping
	public CoursePageDto list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return courses.list(page, size);
	}
	@GetMapping("/{id}")
	public ResponseEntity<CourseDetailDto> detail(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorization) {
		return ResponseEntity.ok(query.course(id, tokens.optional(authorization)));
	}
	@GetMapping("/{id}/lessons")
	public ResponseEntity<List<LessonDto>> lessons(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorization) {
		return ResponseEntity.ok(query.lessons(id, tokens.optional(authorization)));
	}
	@GetMapping("/{id}/progress")
	public ResponseEntity<ProgressResponseDto> progress(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorization) {
		return ResponseEntity.ok(ProgressResponseDto.from(personal.progress(tokens.require(authorization), id)));
	}
	@PostMapping("/{id}/watchlist")
	public ResponseEntity<Void> save(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorization) {
		personal.saveCourse(tokens.require(authorization), id);
		return ResponseEntity.noContent().build();
	}
	@DeleteMapping("/{id}/watchlist")
	public ResponseEntity<Void> remove(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorization) {
		personal.removeCourse(tokens.require(authorization), id);
		return ResponseEntity.noContent().build();
	}
}
