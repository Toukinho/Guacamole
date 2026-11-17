package com.guacamole.backend.services;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.guacamole.backend.dto.ProgressDto;
import com.guacamole.backend.entity.Course;
import com.guacamole.backend.entity.CourseWatchlist;
import com.guacamole.backend.entity.CourseWatchlistId;
import com.guacamole.backend.entity.Lesson;
import com.guacamole.backend.entity.LessonCompletion;
import com.guacamole.backend.entity.LessonCompletionId;
import com.guacamole.backend.entity.User;
import com.guacamole.backend.repositories.CourseRepository;
import com.guacamole.backend.repositories.CourseWatchlistRepository;
import com.guacamole.backend.repositories.LessonCompletionRepository;
import com.guacamole.backend.repositories.LessonRepository;
import com.guacamole.backend.repositories.UserRepository;

/** Gerencia vinculos pessoais e contagens de progresso. O ID recebido deve vir da autenticacao; este servico nao valida tokens. */
@Service
public class UserCourseService {
	private final UserRepository users;
	private final CourseRepository courses;
	private final LessonRepository lessons;
	private final CourseWatchlistRepository watchlist;
	private final LessonCompletionRepository completions;
	public UserCourseService(UserRepository users, CourseRepository courses, LessonRepository lessons, CourseWatchlistRepository watchlist, LessonCompletionRepository completions) {
		this.users = users; this.courses = courses; this.lessons = lessons; this.watchlist = watchlist; this.completions = completions;
	}
	private User lockUser(Long id) {
		return users.lockById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
	}
	private Course course(Long id) {
		return courses.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
	}
	private Lesson lesson(Long id) {
		return lessons.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
	}
	/** Salva um curso para o usuario autenticado; repetir a operacao nao cria outro vinculo. */
	@Transactional
	public void saveCourse(Long authenticatedUserId, Long courseId) {
		User user = lockUser(authenticatedUserId);
		Course course = course(courseId);
		CourseWatchlistId id = new CourseWatchlistId(authenticatedUserId, courseId);
		if (!watchlist.existsById(id)) watchlist.save(new CourseWatchlist(user, course));
	}
	/** Remove o curso salvo, se existir, preservando as conclusoes de aulas. */
	@Transactional
	public void removeCourse(Long authenticatedUserId, Long courseId) {
		lockUser(authenticatedUserId);
		course(courseId);
		watchlist.findById(new CourseWatchlistId(authenticatedUserId, courseId)).ifPresent(watchlist::delete);
	}
	/** Marca uma aula como concluida; repetir a operacao nao cria outro vinculo. */
	@Transactional
	public void completeLesson(Long authenticatedUserId, Long lessonId) {
		User user = lockUser(authenticatedUserId);
		Lesson lesson = lesson(lessonId);
		LessonCompletionId id = new LessonCompletionId(authenticatedUserId, lessonId);
		if (!completions.existsById(id)) completions.save(new LessonCompletion(user, lesson));
	}
	/** Remove a conclusao de uma aula, se existir. */
	@Transactional
	public void removeCompletion(Long authenticatedUserId, Long lessonId) {
		lockUser(authenticatedUserId);
		lesson(lessonId);
		completions.findById(new LessonCompletionId(authenticatedUserId, lessonId)).ifPresent(completions::delete);
	}
	/** Informa se o par usuario e curso esta salvo. */
	@Transactional(readOnly = true)
	public boolean isSaved(Long authenticatedUserId, Long courseId) {
		return watchlist.existsById(new CourseWatchlistId(authenticatedUserId, courseId));
	}
	/** Informa se o par usuario e aula esta concluido. */
	@Transactional(readOnly = true)
	public boolean isCompleted(Long authenticatedUserId, Long lessonId) {
		return completions.existsById(new LessonCompletionId(authenticatedUserId, lessonId));
	}
	/** Retorna as contagens de aulas concluidas e existentes; nao calcula porcentagem. */
	@Transactional(readOnly = true)
	public ProgressDto progress(Long authenticatedUserId, Long courseId) {
		course(courseId);
		return new ProgressDto(completions.countByUserIdAndLessonCourseId(authenticatedUserId, courseId), lessons.countByCourseId(courseId));
	}
	/** Lista os vinculos de cursos salvos pelo usuario. */
	@Transactional(readOnly = true)
	public List<CourseWatchlist> watchlist(Long authenticatedUserId) {
		return watchlist.findByUserId(authenticatedUserId);
	}
}
