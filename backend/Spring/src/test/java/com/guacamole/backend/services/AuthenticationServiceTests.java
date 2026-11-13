package com.guacamole.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.guacamole.backend.dto.LoginRequest;
import com.guacamole.backend.dto.RegisterRequest;
import com.guacamole.backend.entity.User;
import com.guacamole.backend.repositories.UserRepository;

class AuthenticationServiceTests {
	@Test
	void hashesPasswordsWithSaltAndRejectsIncorrectPasswords() {
		PasswordService passwords = new PasswordService();
		String first = passwords.hash("senha-exemplo");
		String second = passwords.hash("senha-exemplo");
		assertNotEquals(first, second);
		assertTrue(passwords.matches("senha-exemplo", first));
		assertFalse(passwords.matches("outra", first));
		assertFalse(passwords.matches("senha-exemplo", "not-a-valid-hash"));
	}

	@Test
	void loginRejectsWrongPasswordAndIssuesRevocableToken() {
		UserRepository users = mock(UserRepository.class);
		PasswordService passwords = new PasswordService();
		TokenService tokens = new TokenService();
		User user = mock(User.class);
		when(user.getId()).thenReturn(3L);
		when(user.getPassword()).thenReturn(passwords.hash("senha-correta"));
		when(users.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
		AuthenticationService service = new AuthenticationService(users, passwords, tokens);
		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> service.login(new LoginRequest("ana@example.com", "senha-incorreta")));
		assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
		String authorization = "Bearer " + service.login(new LoginRequest("ana@example.com", "senha-correta")).token();
		assertEquals(3L, tokens.require(authorization));
		tokens.revoke(authorization);
		assertThrows(ResponseStatusException.class, () -> tokens.require(authorization));
	}

	@Test
	void registrationRejectsExistingEmail() {
		UserRepository users = mock(UserRepository.class);
		when(users.findByEmail("ana@example.com")).thenReturn(Optional.of(new User()));
		AuthenticationService service = new AuthenticationService(users, new PasswordService(), new TokenService());
		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> service.register(new RegisterRequest("Ana", "ana@example.com", "senha")));
		assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
		verify(users, never()).saveAndFlush(any());
	}
}
