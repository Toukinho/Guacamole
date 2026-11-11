package com.guacamole.backend.entity;

import java.io.Serializable;
import java.util.Objects;

public class CourseWatchlistId implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long user;
	private Long course;

	public CourseWatchlistId() {
	}

	public CourseWatchlistId(Long user, Long course) {
		this.user = user;
		this.course = course;
	}

	public Long getUser() {
		return user;
	}

	public Long getCourse() {
		return course;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CourseWatchlistId that)) {
			return false;
		}
		return Objects.equals(user, that.user) && Objects.equals(course, that.course);
	}

	@Override
	public int hashCode() {
		return Objects.hash(user, course);
	}
}
