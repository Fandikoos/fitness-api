package com.fitness.workout.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProgramResponse(
        Long id,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt,
        List<RoutineResponse> routines
) {
}
