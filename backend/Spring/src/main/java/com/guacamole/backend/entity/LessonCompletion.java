package com.guacamole.backend.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Conclusao de uma aula por um usuario, identificada pelo par de IDs e com data de conclusao. */
@Entity
@Table(name = "lesson_completion")
@IdClass(LessonCompletionId.class)
public class LessonCompletion {

	@Id
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, updatable = false)
	private User user;

	@Id
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "lesson_id", nullable = false, updatable = false)
	private Lesson lesson;

	@Column(name = "completed_at", nullable = false, updatable = false)
	private Instant completedAt;

	public LessonCompletion() {
	}

	public LessonCompletion(User user, Lesson lesson) {
		this.user = user;
		this.lesson = lesson;
	}

	@PrePersist
	void initializeTimestamp() {
		if (completedAt == null) {
			completedAt = Instant.now();
		}
	}

	public User getUser() {
		return user;
	}

	public Lesson getLesson() {
		return lesson;
	}

	public Instant getCompletedAt() {
		return completedAt;
	}
}
