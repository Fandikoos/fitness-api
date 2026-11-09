package com.fitness.workout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record WorkoutSessionRequest(

        @NotNull(message = "La fecha de la sesión no puede ser nula")
        @PastOrPresent(message = "La fecha de la sesión no puede ser futura")
        LocalDate sessionDate,

        @NotNull(message = "El ID de la rutina no puede ser nulo")
        Long routineId,

        @Size(max = 500, message = "Las notas no pueden exceder los 500 caracteres")
        String notes,

        @NotEmpty(message = "Debe haber al menos un ejercicio registrado en la sesión")
        @Valid
        List<LoggedExerciseRequest> exercises
) {
}
