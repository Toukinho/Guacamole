package com.guacamole.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CourseDetailDto(Long id, String title, String description,
		@JsonProperty("youtube_id_playlist") String youtubeIdPlaylist, String teacher,
		@JsonProperty("total_lessons") long totalLessons, Boolean saved) { }
