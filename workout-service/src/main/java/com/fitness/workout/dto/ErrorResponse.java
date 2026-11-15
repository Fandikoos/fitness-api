package com.fitness.workout.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

// errors es null en errores genéricos (404, 500...) y contiene los mensajes
// por campo cuando falla la validación de Bean Validation (@NotBlank, @Min, etc.)
public record ErrorResponse(
        int status,
        String error,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<String> errors,
        Instant timestamp,
        String path
) {
}
