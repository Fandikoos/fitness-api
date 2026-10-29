package com.fitness.workout.exception;

/**
 * Se lanza cuando la operación entra en conflicto con el estado actual de la BD.
 * A diferencia de BusinessValidationException (422, error dentro del propio request),
 * esta excepción representa un choque con un recurso ya existente → HTTP 409 Conflict.
 *
 * Ejemplo: intentar crear una rutina para un día que ya tiene rutina en ese programa.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
