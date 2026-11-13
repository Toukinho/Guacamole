package com.guacamole.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LessonDto(Long id, String title, @JsonProperty("youtube_id_video") String youtubeIdVideo,
		int position, @JsonInclude(JsonInclude.Include.NON_NULL) CourseSummaryDto course, Boolean completed) { }
