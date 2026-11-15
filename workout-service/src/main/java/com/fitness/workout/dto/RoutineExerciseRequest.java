package com.fitness.workout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RoutineExerciseRequest(

        // ID del ejercicio del catálogo que se quiere añadir a la rutina
        @NotNull(message = "El id del ejercicio del catálogo es obligatorio")
        Long exerciseTemplateId,

        @NotNull(message = "El orden dentro de la rutina es obligatorio")
        @Min(value = 1, message = "El orden debe ser al menos 1")
        Integer orderInRoutine,

        // Notas específicas para esta rutina (ej. "Agarre cerrado")
        // No se utiliza por ahora, pero se deja preparado para posibles futuras mejoras
        String notes,

        // @Valid propaga la validación a los objetos hijos (ExerciseSetRequest).
        // Sin @Valid, Spring no validaría las anotaciones dentro de cada set.
        @Valid
        List<ExerciseSetRequest> sets
) {
}
