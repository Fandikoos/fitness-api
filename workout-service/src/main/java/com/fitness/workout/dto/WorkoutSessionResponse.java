package com.fitness.workout.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record WorkoutSessionResponse(

        Long id,
        LocalDate sessionDate,
        // Solo el id: la rutina puede cambiar o borrarse después.
        // routineName es el nombre tal como estaba el día del entreno.
        Long routineId,
        String routineName,
        String notes,
        LocalDateTime createdAt,
        List<LoggedExerciseResponse> exercises
        ) {
}
