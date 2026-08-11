package com.fitness.workout.dto;

import com.fitness.workout.enums.DayOfWeek;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record RoutineRequest(

        @NotBlank(message = "El nombre de la rutina es obligatorio")
        String name,

        // Día de la semana opcional
        DayOfWeek dayOfWeek,

        // Descripción opcional
        String description,

        // ID del programa al que pertenece esta rutina (opcional)
        Long programId,

        // @Valid propaga la validación a cada RoutineExerciseRequest de la lista
        @Valid
        List<RoutineExerciseRequest> exercises
) {
}
