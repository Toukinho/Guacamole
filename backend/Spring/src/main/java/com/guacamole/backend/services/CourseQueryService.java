package com.guacamole.backend.services;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.guacamole.backend.dto.*;
import com.guacamole.backend.entity.Course;
import com.guacamole.backend.entity.Lesson;
import com.guacamole.backend.repositories.LessonRepository;
import com.guacamole.backend.repositories.LessonCompletionRepository;

/** Monta respostas dentro de uma transacao para acessar relacionamentos carregados sob demanda. */
@Service
public class CourseQueryService {
	private final CourseService courses;
	private final LessonService lessons;
	private final UserCourseService personal;
	private final LessonRepository lessonRepository;
	private final LessonCompletionRepository completions;

	public CourseQueryService(CourseService courses, LessonService lessons, UserCourseService personal, LessonRepository lessonRepository, LessonCompletionRepository completions) {
		this.courses = courses;
		this.lessons = lessons;
		this.personal = personal;
		this.lessonRepository = lessonRepository;
		this.completions = completions;
	}

	@Transactional(readOnly = true)
	public CourseDetailDto course(Long id, Long userId) {
		Course course = courses.findById(id);
		return new CourseDetailDto(course.getId(), course.getTitle(), course.getDescription(), course.getYoutubeIdPlaylist(),
				course.getTeacher().getName(), lessonRepository.countByCourseId(id), userId != null && personal.isSaved(userId, id));
	}

	@Transactional(readOnly = true)
	public List<LessonDto> lessons(Long courseId, Long userId) {
		List<Lesson> found = lessons.findByCourse(courseId);
		Set<Long> completed = userId == null ? Set.of() : Set.copyOf(completions.findCompletedLessonIds(userId, courseId));
		return found.stream().map(lesson -> new LessonDto(lesson.getId(), lesson.getTitle(), lesson.getYoutubeIdVideo(),
				lesson.getPosition(), null, completed.contains(lesson.getId()))).toList();
	}

	@Transactional(readOnly = true)
	public LessonDto lesson(Long id, Long userId) {
		return lessonDto(lessons.findById(id), userId, true);
	}

	private LessonDto lessonDto(Lesson lesson, Long userId, boolean includeCourse) {
		Course course = lesson.getCourse();
		CourseSummaryDto summary = includeCourse ? new CourseSummaryDto(course.getId(), course.getTitle(), course.getTeacher().getName()) : null;
		return new LessonDto(lesson.getId(), lesson.getTitle(), lesson.getYoutubeIdVideo(), lesson.getPosition(), summary,
				userId != null && personal.isCompleted(userId, lesson.getId()));
	}

	@Transactional(readOnly = true)
	public List<WatchlistCourseDto> watchlist(Long userId) {
		Map<Long, Long> totals = counts(lessonRepository.countForWatchlist(userId));
		Map<Long, Long> completed = counts(completions.countCompletedForWatchlist(userId));
		return personal.watchlist(userId).stream().map(entry -> {
			Course course = entry.getCourse();
			ProgressResponseDto progress = ProgressResponseDto.from(new ProgressDto(completed.getOrDefault(course.getId(), 0L), totals.getOrDefault(course.getId(), 0L)));
			return new WatchlistCourseDto(course.getId(), course.getTitle(), course.getTeacher().getName(),
					progress.completedLessons(), progress.totalLessons(), progress.percentage());
		}).toList();
	}

	private Map<Long, Long> counts(List<Object[]> rows) {
		return rows.stream().collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
	}
}
