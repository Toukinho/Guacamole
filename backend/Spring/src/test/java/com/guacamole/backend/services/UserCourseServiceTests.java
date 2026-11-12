package com.guacamole.backend.services;

import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.guacamole.backend.entity.Course;
import com.guacamole.backend.entity.CourseWatchlistId;
import com.guacamole.backend.entity.User;
import com.guacamole.backend.repositories.*;

class UserCourseServiceTests {
	@Test
	void savingAnExistingCourseDoesNotCreateAnotherEntry() {
		UserRepository users = mock(UserRepository.class);
		CourseRepository courses = mock(CourseRepository.class);
		LessonRepository lessons = mock(LessonRepository.class);
		CourseWatchlistRepository watchlist = mock(CourseWatchlistRepository.class);
		LessonCompletionRepository completions = mock(LessonCompletionRepository.class);
		when(users.lockById(1L)).thenReturn(Optional.of(new User()));
		when(courses.findById(2L)).thenReturn(Optional.of(new Course()));
		when(watchlist.existsById(new CourseWatchlistId(1L, 2L))).thenReturn(true);
		new UserCourseService(users, courses, lessons, watchlist, completions).saveCourse(1L, 2L);
		verify(watchlist, never()).save(any());
	}
}
