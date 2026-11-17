package com.guacamole.backend.entity;

import java.io.Serializable;
import java.util.Objects;

/** Identificador composto pelos IDs do usuario e da aula concluida. */
public class LessonCompletionId implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long user;
	private Long lesson;

	public LessonCompletionId() {
	}

	public LessonCompletionId(Long user, Long lesson) {
		this.user = user;
		this.lesson = lesson;
	}

	public Long getUser() {
		return user;
	}

	public Long getLesson() {
		return lesson;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof LessonCompletionId that)) {
			return false;
		}
		return Objects.equals(user, that.user) && Objects.equals(lesson, that.lesson);
	}

	@Override
	public int hashCode() {
		return Objects.hash(user, lesson);
	}
}
