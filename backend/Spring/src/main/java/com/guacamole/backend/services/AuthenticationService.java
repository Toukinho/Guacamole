package com.guacamole.backend.services;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.guacamole.backend.dto.*;
import com.guacamole.backend.entity.User;
import com.guacamole.backend.repositories.UserRepository;

@Service
public class AuthenticationService {
	private final UserRepository users;
	private final PasswordService passwords;
	private final TokenService tokens;

	public AuthenticationService(UserRepository users, PasswordService passwords, TokenService tokens) {
		this.users = users;
		this.passwords = passwords;
		this.tokens = tokens;
	}

	@Transactional
	public TokenDto register(RegisterRequest request) {
		if (users.findByEmail(request.email()).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
		User user = new User(request.name(), request.email(), passwords.hash(request.password()));
		try {
			users.saveAndFlush(user);
		} catch (DataIntegrityViolationException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered", ex);
		}
		return new TokenDto(tokens.issue(user.getId()));
	}

	@Transactional(readOnly = true)
	public TokenDto login(LoginRequest request) {
		User user = users.findByEmail(request.email()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
		if (!passwords.matches(request.password(), user.getPassword())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
		return new TokenDto(tokens.issue(user.getId()));
	}

	@Transactional(readOnly = true)
	public UserDto me(Long userId) {
		User user = users.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
		return new UserDto(user.getId(), user.getName(), user.getEmail());
	}
}
