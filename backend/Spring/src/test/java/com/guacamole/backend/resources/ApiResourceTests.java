package com.guacamole.backend.resources;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import com.guacamole.backend.dto.*;
import com.guacamole.backend.services.*;

class ApiResourceTests {
	private final CourseService courses = mock(CourseService.class);
	private final CourseQueryService query = mock(CourseQueryService.class);
	private final UserCourseService personal = mock(UserCourseService.class);
	private final AuthenticationService authentication = mock(AuthenticationService.class);
	private final TokenService tokens = new TokenService();
	private MockMvc mvc;
	private String bearer;

	@BeforeEach
	void setUp() {
		bearer = "Bearer " + tokens.issue(7L);
		mvc = MockMvcBuilders.standaloneSetup(new CourseResource(courses, query, personal, tokens),
				new LessonResource(query, personal, tokens), new AuthenticationResource(authentication, tokens),
				new UserResource(authentication, query, tokens)).build();
	}

	@Test
	void listsCoursesAndRejectsInvalidPagination() throws Exception {
		when(courses.list(0, 20)).thenReturn(new CoursePageDto(List.of(new CourseSummaryDto(1L, "Java", "Ana")), 0, 20, 1, 1));
		when(courses.list(-1, 20)).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));
		mvc.perform(get("/courses")).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].teacher").value("Ana"));
		mvc.perform(get("/courses?page=-1")).andExpect(status().isBadRequest());
	}

	@Test
	void courseAndLessonDetailsUseDocumentedFields() throws Exception {
		when(query.course(1L, 7L)).thenReturn(new CourseDetailDto(1L, "Java", "Curso", "playlist", "Ana", 2, true));
		when(query.lessons(1L, 7L)).thenReturn(List.of(new LessonDto(2L, "Aula", "video", 1, null, true)));
		when(query.lesson(2L, 7L)).thenReturn(new LessonDto(2L, "Aula", "video", 1, new CourseSummaryDto(1L, "Java", "Ana"), true));
		mvc.perform(get("/courses/1").header("Authorization", bearer)).andExpect(status().isOk())
				.andExpect(jsonPath("$.youtube_id_playlist").value("playlist")).andExpect(jsonPath("$.total_lessons").value(2)).andExpect(jsonPath("$.saved").value(true));
		mvc.perform(get("/courses/1/lessons").header("Authorization", bearer)).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].youtube_id_video").value("video")).andExpect(jsonPath("$[0].completed").value(true));
		mvc.perform(get("/lessons/2").header("Authorization", bearer)).andExpect(status().isOk()).andExpect(jsonPath("$.course.title").value("Java"));
	}

	@Test
	void returnsNotFoundForMissingCourseAndLesson() throws Exception {
		when(query.course(eq(99L), any())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
		when(query.lesson(eq(99L), any())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
		when(query.lessons(eq(99L), any())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
		doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(personal).saveCourse(7L, 99L);
		doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(personal).completeLesson(7L, 99L);
		mvc.perform(get("/courses/99").header("Authorization", bearer)).andExpect(status().isNotFound());
		mvc.perform(get("/lessons/99").header("Authorization", bearer)).andExpect(status().isNotFound());
		mvc.perform(get("/courses/99/lessons").header("Authorization", bearer)).andExpect(status().isNotFound());
		mvc.perform(post("/courses/99/watchlist").header("Authorization", bearer)).andExpect(status().isNotFound());
		mvc.perform(post("/lessons/99/complete").header("Authorization", bearer)).andExpect(status().isNotFound());
	}

	@Test
	void personalOperationsTakeIdentityOnlyFromToken() throws Exception {
		when(personal.progress(7L, 1L)).thenReturn(new ProgressDto(2, 4));
		mvc.perform(get("/courses/1/progress").header("Authorization", bearer)).andExpect(status().isOk())
				.andExpect(jsonPath("$.completed_lessons").value(2)).andExpect(jsonPath("$.percentage").value(50.0));
		mvc.perform(post("/courses/1/watchlist?user_id=999").header("Authorization", bearer)).andExpect(status().isNoContent());
		mvc.perform(delete("/courses/1/watchlist").header("Authorization", bearer)).andExpect(status().isNoContent());
		mvc.perform(post("/lessons/2/complete").header("Authorization", bearer)).andExpect(status().isNoContent());
		mvc.perform(delete("/lessons/2/complete").header("Authorization", bearer)).andExpect(status().isNoContent());
		verify(personal).saveCourse(7L, 1L);
		verify(personal).removeCourse(7L, 1L);
		verify(personal).completeLesson(7L, 2L);
		verify(personal).removeCompletion(7L, 2L);
		mvc.perform(post("/lessons/2/complete")).andExpect(status().isUnauthorized());
		mvc.perform(get("/me").header("Authorization", "Bearer invalid")).andExpect(status().isUnauthorized());
	}

	@Test
	void returnsCurrentUserAndWatchlist() throws Exception {
		when(authentication.me(7L)).thenReturn(new UserDto(7L, "Ana", "ana@example.com"));
		when(query.watchlist(7L)).thenReturn(List.of(new WatchlistCourseDto(1L, "Java", "Ana", 1, 2, 50)));
		mvc.perform(get("/me").header("Authorization", bearer)).andExpect(status().isOk()).andExpect(jsonPath("$.email").value("ana@example.com")).andExpect(jsonPath("$.password").doesNotExist());
		mvc.perform(get("/me/watchlist").header("Authorization", bearer)).andExpect(status().isOk()).andExpect(jsonPath("$[0].completed_lessons").value(1));
	}

	@Test
	void validatesRequestsAndRevokesTokenOnLogout() throws Exception {
		when(authentication.register(any())).thenReturn(new TokenDto("test-token"));
		when(authentication.login(any())).thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
		mvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Ana\",\"email\":\"ana@example.com\",\"password\":\"secret\"}"))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.token").value("test-token"));
		mvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\",\"email\":\"invalid\",\"password\":\"\"}"))
				.andExpect(status().isBadRequest());
		mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"ana@example.com\",\"password\":\"wrong\"}"))
				.andExpect(status().isUnauthorized());
		mvc.perform(post("/logout").header("Authorization", bearer)).andExpect(status().isNoContent());
		mvc.perform(get("/me").header("Authorization", bearer)).andExpect(status().isUnauthorized());
	}

	@Test
	void duplicateEmailReturnsConflictAndEmptyCoursesHaveZeroProgress() throws Exception {
		when(authentication.register(any())).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT));
		when(personal.progress(7L, 1L)).thenReturn(new ProgressDto(0, 0));
		mvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Ana\",\"email\":\"ana@example.com\",\"password\":\"secret\"}"))
				.andExpect(status().isConflict());
		mvc.perform(get("/courses/1/progress").header("Authorization", bearer)).andExpect(status().isOk())
				.andExpect(jsonPath("$.total_lessons").value(0)).andExpect(jsonPath("$.percentage").value(0));
	}
}
