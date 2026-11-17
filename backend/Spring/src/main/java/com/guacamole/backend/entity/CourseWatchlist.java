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

/** Vinculo entre usuario e curso salvo, identificado pelo par de IDs e com data de criacao. */
@Entity
@Table(name = "course_watchlist")
@IdClass(CourseWatchlistId.class)
public class CourseWatchlist {

	@Id
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, updatable = false)
	private User user;

	@Id
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "course_id", nullable = false, updatable = false)
	private Course course;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public CourseWatchlist() {
	}

	public CourseWatchlist(User user, Course course) {
		this.user = user;
		this.course = course;
	}

	@PrePersist
	void initializeTimestamp() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public User getUser() {
		return user;
	}

	public Course getCourse() {
		return course;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
