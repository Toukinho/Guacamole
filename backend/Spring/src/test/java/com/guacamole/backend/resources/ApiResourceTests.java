package com.guacamole.backend.resources;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import com.guacamole.backend.services.RevokedTokenCleanup;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiResourceTests {
	@Autowired MockMvc mvc;
	@Autowired JdbcTemplate jdbc;
	@Autowired EntityManager entityManager;
	@Autowired EntityManagerFactory entityManagerFactory;
	@Autowired RevokedTokenCleanup cleanup;

	private String register(String email) throws Exception {
		String result = mvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Ana\",\"email\":\"" + email + "\",\"password\":\"strong-test-password\"}"))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.password").doesNotExist())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(result, "$.token");
	}

	@Test
	void registersAuthenticatesAndRevokesJwt() throws Exception {
		String email = UUID.randomUUID() + "@example.com";
		String token = register(email);
		assertTrue(jdbc.queryForObject("select password from `user` where email = ?", String.class, email).startsWith("$2"));
		mvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Ana\",\"email\":\"" + email + "\",\"password\":\"strong-test-password\"}"))
				.andExpect(status().isConflict());
		mvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"\",\"email\":\"invalid\",\"password\":\"\"}"))
				.andExpect(status().isBadRequest());
		mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"wrong\"}"))
				.andExpect(status().isUnauthorized());
		mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"strong-test-password\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.token").exists());
		mvc.perform(get("/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email)).andExpect(jsonPath("$.password").doesNotExist());
		mvc.perform(post("/logout").header("Authorization", "Bearer " + token)).andExpect(status().isNoContent());
		mvc.perform(get("/me").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
	}

	@Test
	void rejectsWeakPasswordsAndThrottlesLogin() throws Exception {
		String email = UUID.randomUUID() + "@example.com";
		mvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Ana\",\"email\":\"" + email + "\",\"password\":\"short\"}"))
				.andExpect(status().isBadRequest());
		for (int i = 0; i < 5; i++) {
			mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
					.content("{\"email\":\"" + email + "\",\"password\":\"incorrect\"}"))
					.andExpect(status().isUnauthorized());
		}
		mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"incorrect\"}"))
				.andExpect(status().isTooManyRequests());
	}

	@Test
	void cleansOnlyExpiredRevocationsAndKeepsLoggedOutTokensRejected() throws Exception {
		String token = register(UUID.randomUUID() + "@example.com");
		mvc.perform(post("/logout").header("Authorization", "Bearer " + token)).andExpect(status().isNoContent());
		entityManager.flush();
		String expiredId = UUID.randomUUID().toString();
		jdbc.update("insert into revoked_token(id, expires_at) values (?, ?)", expiredId, java.sql.Timestamp.from(Instant.now().minusSeconds(60)));
		cleanup.cleanExpired();
		assertEquals(0, jdbc.queryForObject("select count(*) from revoked_token where id = ?", Integer.class, expiredId));
		mvc.perform(get("/me").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
	}

	@Test
	void lessonAndWatchlistQueriesStayBoundedAsDataGrows() throws Exception {
		String token = register(UUID.randomUUID() + "@example.com");
		String title = UUID.randomUUID().toString();
		jdbc.update("insert into teacher(name) values (?)", title);
		Long teacher = jdbc.queryForObject("select id from teacher where name = ?", Long.class, title);
		jdbc.update("insert into course(teacher_id, title) values (?, ?)", teacher, title);
		Long course = jdbc.queryForObject("select id from course where title = ?", Long.class, title);
		jdbc.update("insert into course(teacher_id, title) values (?, ?)", teacher, title + "-empty");
		Long emptyCourse = jdbc.queryForObject("select id from course where title = ?", Long.class, title + "-empty");
		for (int i = 1; i <= 4; i++) jdbc.update("insert into lesson(course_id, title, position) values (?, ?, ?)", course, title, i);
		Long lesson = jdbc.queryForObject("select id from lesson where course_id = ? and position = 1", Long.class, course);
		mvc.perform(post("/courses/" + course + "/watchlist").header("Authorization", "Bearer " + token)).andExpect(status().isNoContent());
		mvc.perform(post("/courses/" + emptyCourse + "/watchlist").header("Authorization", "Bearer " + token)).andExpect(status().isNoContent());
		mvc.perform(post("/lessons/" + lesson + "/complete").header("Authorization", "Bearer " + token)).andExpect(status().isNoContent());
		entityManager.flush();
		entityManager.clear();
		var stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
		stats.setStatisticsEnabled(true);
		stats.clear();
		mvc.perform(get("/courses/" + course + "/lessons").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].completed").value(true))
				.andExpect(jsonPath("$[1].completed").value(false));
		assertTrue(stats.getPrepareStatementCount() <= 5, "lesson queries: " + stats.getPrepareStatementCount());
		entityManager.clear();
		stats.clear();
		String watchlist = mvc.perform(get("/me/watchlist").header("Authorization", "Bearer " + token)).andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		assertTrue(watchlist.contains("\"completed_lessons\":1,\"total_lessons\":4"));
		assertTrue(watchlist.contains("\"completed_lessons\":0,\"total_lessons\":0"));
		assertTrue(stats.getPrepareStatementCount() <= 5, "watchlist queries: " + stats.getPrepareStatementCount());
	}

	@Test
	void rejectsMissingInvalidAndExpiredTokens() throws Exception {
		mvc.perform(get("/courses")).andExpect(status().isOk());
		mvc.perform(get("/me")).andExpect(status().isUnauthorized());
		mvc.perform(get("/courses/1/progress")).andExpect(status().isUnauthorized());
		mvc.perform(post("/courses/1/watchlist")).andExpect(status().isUnauthorized());
		mvc.perform(post("/lessons/1/complete")).andExpect(status().isUnauthorized());
		mvc.perform(get("/me").header("Authorization", "Bearer invalid")).andExpect(status().isUnauthorized());
		String expired = Jwts.builder().subject("1").id(UUID.randomUUID().toString())
				.expiration(Date.from(Instant.now().minusSeconds(60)))
				.signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="))).compact();
		mvc.perform(get("/me").header("Authorization", "Bearer " + expired)).andExpect(status().isUnauthorized());
	}

	@Test
	void personalFlagsAndMutationsAreScopedToTokenUser() throws Exception {
		String first = register(UUID.randomUUID() + "@example.com");
		String second = register(UUID.randomUUID() + "@example.com");
		String title = UUID.randomUUID().toString();
		jdbc.update("insert into teacher(name) values (?)", title);
		Long teacher = jdbc.queryForObject("select id from teacher where name = ?", Long.class, title);
		jdbc.update("insert into course(teacher_id, title) values (?, ?)", teacher, title);
		Long course = jdbc.queryForObject("select id from course where title = ?", Long.class, title);
		jdbc.update("insert into lesson(course_id, title, position) values (?, ?, 1)", course, title);
		Long lesson = jdbc.queryForObject("select id from lesson where course_id = ?", Long.class, course);
		String saved = "/courses/" + course + "/watchlist?user_id=999";
		String completed = "/lessons/" + lesson + "/complete?user_id=999";
		for (int i = 0; i < 2; i++) {
			mvc.perform(post(saved).header("Authorization", "Bearer " + first)).andExpect(status().isNoContent());
			mvc.perform(post(completed).header("Authorization", "Bearer " + first)).andExpect(status().isNoContent());
		}
		entityManager.flush();
		assertEquals(1, jdbc.queryForObject("select count(*) from course_watchlist where course_id = ?", Integer.class, course));
		assertEquals(1, jdbc.queryForObject("select count(*) from lesson_completion where lesson_id = ?", Integer.class, lesson));
		mvc.perform(get("/courses/" + course)).andExpect(status().isOk()).andExpect(jsonPath("$.saved").value(false));
		mvc.perform(get("/lessons/" + lesson)).andExpect(status().isOk()).andExpect(jsonPath("$.completed").value(false));
		mvc.perform(get("/courses/" + course).header("Authorization", "Bearer " + first)).andExpect(jsonPath("$.saved").value(true));
		mvc.perform(get("/courses/" + course + "/lessons").header("Authorization", "Bearer " + first)).andExpect(jsonPath("$[0].completed").value(true));
		mvc.perform(get("/courses/" + course).header("Authorization", "Bearer " + second)).andExpect(jsonPath("$.saved").value(false));
		mvc.perform(delete(saved).header("Authorization", "Bearer " + second)).andExpect(status().isNoContent());
		mvc.perform(delete(completed).header("Authorization", "Bearer " + second)).andExpect(status().isNoContent());
		mvc.perform(get("/lessons/" + lesson).header("Authorization", "Bearer " + first)).andExpect(jsonPath("$.completed").value(true));
		mvc.perform(get("/courses/" + course).header("Authorization", "Bearer " + first)).andExpect(jsonPath("$.saved").value(true));
	}
}
