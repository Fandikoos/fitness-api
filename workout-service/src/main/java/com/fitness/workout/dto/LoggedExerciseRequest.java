package com.fitness.workout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record LoggedExerciseRequest (

        @NotNull(message = "El ID de la plantilla de ejercicio no puede ser nulo")
        Long exerciseTemplateId,

        @NotNull(message = "El orden del ejercicio en la sesión no puede ser nulo")
        @Min(value = 1, message = "El orden del ejercicio en la sesión debe ser al menos 1")
        Integer orderInSession,

        @Size(max = 255, message = "Las notas no pueden exceder los 255 caracteres")
        String notes,

        @NotEmpty(message = "Debe haber al menos una serie registrada para el ejercicio")
        @Valid
        List<LoggedSetRequest> sets
) {
}
