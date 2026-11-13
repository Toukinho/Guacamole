package com.guacamole.backend.services;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.guacamole.backend.dto.*;
import com.guacamole.backend.entity.Course;
import com.guacamole.backend.entity.Lesson;
import com.guacamole.backend.repositories.LessonRepository;

/** Monta respostas dentro de uma transacao para acessar relacionamentos carregados sob demanda. */
@Service
public class CourseQueryService {
	private final CourseService courses;
	private final LessonService lessons;
	private final UserCourseService personal;
	private final LessonRepository lessonRepository;

	public CourseQueryService(CourseService courses, LessonService lessons, UserCourseService personal, LessonRepository lessonRepository) {
		this.courses = courses;
		this.lessons = lessons;
		this.personal = personal;
		this.lessonRepository = lessonRepository;
	}

	@Transactional(readOnly = true)
	public CourseDetailDto course(Long id, Long userId) {
		Course course = courses.findById(id);
		return new CourseDetailDto(course.getId(), course.getTitle(), course.getDescription(), course.getYoutubeIdPlaylist(),
				course.getTeacher().getName(), lessonRepository.countByCourseId(id), userId == null ? null : personal.isSaved(userId, id));
	}

	@Transactional(readOnly = true)
	public List<LessonDto> lessons(Long courseId, Long userId) {
		return lessons.findByCourse(courseId).stream().map(lesson -> lessonDto(lesson, userId, false)).toList();
	}

	@Transactional(readOnly = true)
	public LessonDto lesson(Long id, Long userId) {
		return lessonDto(lessons.findById(id), userId, true);
	}

	private LessonDto lessonDto(Lesson lesson, Long userId, boolean includeCourse) {
		Course course = lesson.getCourse();
		CourseSummaryDto summary = includeCourse ? new CourseSummaryDto(course.getId(), course.getTitle(), course.getTeacher().getName()) : null;
		return new LessonDto(lesson.getId(), lesson.getTitle(), lesson.getYoutubeIdVideo(), lesson.getPosition(), summary,
				userId == null ? null : personal.isCompleted(userId, lesson.getId()));
	}

	@Transactional(readOnly = true)
	public List<WatchlistCourseDto> watchlist(Long userId) {
		return personal.watchlist(userId).stream().map(entry -> {
			Course course = entry.getCourse();
			ProgressResponseDto progress = ProgressResponseDto.from(personal.progress(userId, course.getId()));
			return new WatchlistCourseDto(course.getId(), course.getTitle(), course.getTeacher().getName(),
					progress.completedLessons(), progress.totalLessons(), progress.percentage());
		}).toList();
	}
}
