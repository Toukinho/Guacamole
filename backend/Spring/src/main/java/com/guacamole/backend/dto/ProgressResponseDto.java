package com.guacamole.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProgressResponseDto(@JsonProperty("completed_lessons") long completedLessons,
		@JsonProperty("total_lessons") long totalLessons, double percentage) {
	public static ProgressResponseDto from(ProgressDto progress) {
		double percentage = progress.totalLessons() == 0 ? 0 : 100.0 * progress.completedLessons() / progress.totalLessons();
		return new ProgressResponseDto(progress.completedLessons(), progress.totalLessons(), percentage);
	}
}
