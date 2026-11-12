package com.guacamole.backend.resources;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.guacamole.backend.dto.CoursePageDto;
import com.guacamole.backend.services.CourseService;

@RestController
@RequestMapping("/courses")
public class CourseResource {
	private final CourseService courses;
	public CourseResource(CourseService courses) { this.courses = courses; }
	@GetMapping
	public CoursePageDto list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return courses.list(page, size);
	}
}
