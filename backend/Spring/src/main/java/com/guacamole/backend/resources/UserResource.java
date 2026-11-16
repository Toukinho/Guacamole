package com.guacamole.backend.resources;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.guacamole.backend.dto.UserDto;
import com.guacamole.backend.dto.WatchlistCourseDto;
import com.guacamole.backend.services.AuthenticationService;
import com.guacamole.backend.services.CourseQueryService;

@RestController
@RequestMapping("/me")
public class UserResource {
	private final AuthenticationService authentication;
	private final CourseQueryService query;
	public UserResource(AuthenticationService authentication, CourseQueryService query) {
		this.authentication = authentication; this.query = query;
	}
	@GetMapping
	public ResponseEntity<UserDto> me(@AuthenticationPrincipal Long userId) {
		return ResponseEntity.ok(authentication.me(userId));
	}
	@GetMapping("/watchlist")
	public ResponseEntity<List<WatchlistCourseDto>> watchlist(@AuthenticationPrincipal Long userId) {
		return ResponseEntity.ok(query.watchlist(userId));
	}
}
