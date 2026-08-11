package com.fitness.workout.dto;

import com.fitness.workout.enums.MuscleGroup;

public record ExerciseTemplateResponse(
        Long id,
        String name,
        MuscleGroup muscleGroup,
        String imageUrl,
        String videoUrl
) {
}
