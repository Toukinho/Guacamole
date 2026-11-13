package com.guacamole.backend.resources;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.guacamole.backend.dto.UserDto;
import com.guacamole.backend.dto.WatchlistCourseDto;
import com.guacamole.backend.services.AuthenticationService;
import com.guacamole.backend.services.CourseQueryService;
import com.guacamole.backend.services.TokenService;

@RestController
@RequestMapping("/me")
public class UserResource {
	private final AuthenticationService authentication;
	private final CourseQueryService query;
	private final TokenService tokens;
	public UserResource(AuthenticationService authentication, CourseQueryService query, TokenService tokens) {
		this.authentication = authentication; this.query = query; this.tokens = tokens;
	}
	@GetMapping
	public ResponseEntity<UserDto> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
		return ResponseEntity.ok(authentication.me(tokens.require(authorization)));
	}
	@GetMapping("/watchlist")
	public ResponseEntity<List<WatchlistCourseDto>> watchlist(@RequestHeader(value = "Authorization", required = false) String authorization) {
		return ResponseEntity.ok(query.watchlist(tokens.require(authorization)));
	}
}
