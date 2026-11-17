package com.guacamole.backend.dto;

// Os totais permitem calcular a porcentagem quando a politica for definida.
public record ProgressDto(long completedLessons, long totalLessons) { }
