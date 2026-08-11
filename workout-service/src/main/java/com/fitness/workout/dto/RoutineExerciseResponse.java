package com.fitness.workout.dto;

import java.util.List;

public record RoutineExerciseResponse(
        Long id,
        // Se incluye el template completo para que el frontend tenga acceso
        // al nombre, imagen, vídeo y grupo muscular sin hacer otra petición.
        ExerciseTemplateResponse exerciseTemplate,
        Integer orderInRoutine,
        String notes,
        List<ExerciseSetResponse> sets
) {
}
