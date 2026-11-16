package com.guacamole.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;

class EnvironmentFileTests {
	@Configuration(proxyBeanMethods = false)
	static class ProbeConfiguration { }

	@Test
	void importsDotEnvFromBackendOrRepositoryWorkingDirectory(@TempDir Path directory) throws Exception {
		String properties = Files.readString(Path.of("src/main/resources/application.properties"));
		properties = properties.replace("optional:file:./", "optional:" + directory.toUri());
		properties += "\ndotenv.probe=${GUACAMOLE_TEST_DOTENV_PROBE}\n";
		Path config = directory.resolve("application.properties");
		Files.writeString(config, properties);
		Path backendFile = directory.resolve(".env");
		Path repositoryFile = directory.resolve("backend/Spring/.env");
		Files.createDirectories(repositoryFile.getParent());
		Files.writeString(backendFile, "GUACAMOLE_TEST_DOTENV_PROBE=backend\n");
		assertImported(config, "backend");
		Files.delete(backendFile);
		Files.writeString(repositoryFile, "GUACAMOLE_TEST_DOTENV_PROBE=repository\n");
		assertImported(config, "repository");
	}

	private void assertImported(Path config, String expected) {
		try (var context = new SpringApplicationBuilder(ProbeConfiguration.class)
				.web(WebApplicationType.NONE)
				.run("--spring.config.location=" + config.toUri())) {
			assertEquals(expected, context.getEnvironment().getProperty("dotenv.probe"));
		}
	}
}
