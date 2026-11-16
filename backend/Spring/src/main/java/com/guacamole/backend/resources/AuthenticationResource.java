package com.guacamole.backend.resources;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.guacamole.backend.dto.*;
import com.guacamole.backend.services.AuthenticationService;
import com.guacamole.backend.services.TokenService;
import com.guacamole.backend.services.LoginRateLimiter;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class AuthenticationResource {
	private final AuthenticationService authentication;
	private final TokenService tokens;
	private final LoginRateLimiter limiter;
	public AuthenticationResource(AuthenticationService authentication, TokenService tokens, LoginRateLimiter limiter) {
		this.authentication = authentication; this.tokens = tokens; this.limiter = limiter;
	}
	@PostMapping("/register")
	public ResponseEntity<TokenDto> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
		limiter.checkRegistration(httpRequest.getRemoteAddr(), request.email());
		return ResponseEntity.status(HttpStatus.CREATED).body(authentication.register(request));
	}
	@PostMapping("/login")
	public ResponseEntity<TokenDto> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
		limiter.check(httpRequest.getRemoteAddr(), request.email());
		return ResponseEntity.ok(authentication.login(request));
	}
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
		tokens.revoke(authorization);
		return ResponseEntity.noContent().build();
	}
}
