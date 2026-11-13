package com.guacamole.backend.resources;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.guacamole.backend.dto.*;
import com.guacamole.backend.services.AuthenticationService;
import com.guacamole.backend.services.TokenService;

@RestController
public class AuthenticationResource {
	private final AuthenticationService authentication;
	private final TokenService tokens;
	public AuthenticationResource(AuthenticationService authentication, TokenService tokens) {
		this.authentication = authentication; this.tokens = tokens;
	}
	@PostMapping("/register")
	public ResponseEntity<TokenDto> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authentication.register(request));
	}
	@PostMapping("/login")
	public ResponseEntity<TokenDto> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authentication.login(request));
	}
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
		tokens.revoke(authorization);
		return ResponseEntity.noContent().build();
	}
}
