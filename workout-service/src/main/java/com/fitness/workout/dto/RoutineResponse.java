package com.fitness.workout.dto;

import com.fitness.workout.enums.DayOfWeek;

import java.time.LocalDateTime;
import java.util.List;

public record RoutineResponse(
        Long id,
        String name,
        DayOfWeek dayOfWeek,
        String description,
        Long programId,
        LocalDateTime createdAt,
        List<RoutineExerciseResponse> exercises
) {
}
