package com.guacamole.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WatchlistCourseDto(Long id, String title, String teacher,
		@JsonProperty("completed_lessons") long completedLessons,
		@JsonProperty("total_lessons") long totalLessons, double percentage) { }
