package com.guacamole.backend.dto;
import java.util.List;
public record CoursePageDto(List<CourseSummaryDto> content, int page, int size, long totalElements, int totalPages) { }
