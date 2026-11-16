package com.guacamole.backend.services;

import static org.junit.jupiter.api.Assertions.*;
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
	void usesBcryptWithSaltAndRejectsIncorrectPasswords() {
		PasswordService passwords = new PasswordService();
		String first = passwords.hash("senha-exemplo");
		assertTrue(first.startsWith("$2"));
		assertNotEquals(first, passwords.hash("senha-exemplo"));
		assertTrue(passwords.matches("senha-exemplo", first));
		assertFalse(passwords.matches("outra", first));
	}

	@Test
	void rejectsInvalidCredentialsAndDuplicateEmail() {
		UserRepository users = mock(UserRepository.class);
		TokenService tokens = mock(TokenService.class);
		PasswordService passwords = new PasswordService();
		User user = new User("Ana", "ana@example.com", passwords.hash("senha"));
		when(users.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
		AuthenticationService service = new AuthenticationService(users, passwords, tokens);
		assertEquals(HttpStatus.UNAUTHORIZED, assertThrows(ResponseStatusException.class,
				() -> service.login(new LoginRequest("ana@example.com", "errada"))).getStatusCode());
		assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
				() -> service.register(new RegisterRequest("Ana", "ana@example.com", "senha"))).getStatusCode());
		verify(users, never()).saveAndFlush(any());
	}
}
