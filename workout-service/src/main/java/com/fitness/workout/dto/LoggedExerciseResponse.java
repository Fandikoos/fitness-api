package com.fitness.workout.dto;

import java.util.List;

public record LoggedExerciseResponse (
        Long id,
        ExerciseTemplateResponse exerciseTemplate,
        Integer orderInSession,
        String notes,
        List<LoggedSetResponse> sets
) {
}
