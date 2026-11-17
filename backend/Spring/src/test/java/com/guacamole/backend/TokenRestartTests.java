package com.guacamole.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import com.guacamole.backend.entity.User;
import com.guacamole.backend.repositories.UserRepository;
import com.guacamole.backend.services.TokenService;

class TokenRestartTests {
	@Test
	void tokenRemainsValidAfterApplicationRestart() {
		String database = "jdbc:h2:mem:restart_" + UUID.randomUUID().toString().replace("-", "")
				+ ";MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1";
		String token;
		Long userId;
		try (ConfigurableApplicationContext first = start(database)) {
			User user = first.getBean(UserRepository.class).saveAndFlush(new User("Ana", "restart@example.com", "hash"));
			userId = user.getId();
			token = first.getBean(TokenService.class).issue(userId);
		}
		try (ConfigurableApplicationContext second = start(database)) {
			assertEquals(userId, second.getBean(TokenService.class).require("Bearer " + token));
		}
	}

	private ConfigurableApplicationContext start(String database) {
		return new SpringApplicationBuilder(BackendApplication.class).run(
				"--server.port=0",
				"--spring.datasource.url=" + database,
				"--spring.datasource.username=sa",
				"--spring.datasource.password=",
				"--spring.jpa.hibernate.ddl-auto=validate",
				"--spring.flyway.enabled=true");
	}
}
