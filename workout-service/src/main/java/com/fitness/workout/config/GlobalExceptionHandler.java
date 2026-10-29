package com.fitness.workout.config;

import com.fitness.workout.dto.ErrorResponse;
import com.fitness.workout.exception.BusinessValidationException;
import com.fitness.workout.exception.ConflictException;
import com.fitness.workout.exception.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;

// @RestControllerAdvice intercepta excepciones lanzadas en cualquier @RestController
// de la aplicación, centralizando el manejo de errores en un único lugar.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                null,                          // sin lista de errores de campo
                LocalDateTime.now(),
                request.getDescription(false)  // "uri=/api/routines/5"
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // 409 Conflict: la operación choca con el estado actual de la BD
    // (ej: ya existe una rutina para ese día en ese programa).
    // También capturamos DataIntegrityViolationException como red de seguridad:
    // si alguna constraint de BD (UNIQUE, FK) falla y se nos escapa la validación
    // previa, devolvemos 409 en lugar de un 500 genérico.
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                null,
                LocalDateTime.now(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "La operación viola una restricción de integridad de datos",
                null,
                LocalDateTime.now(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // 422 Unprocessable Entity: el request está bien formado (no es un 400),
    // pero viola una regla de negocio del dominio.
    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessValidation(
            BusinessValidationException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Unprocessable Entity",
                ex.getMessage(),
                null,
                LocalDateTime.now(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // Spring lanza MethodArgumentNotValidException cuando @Valid falla en un @RequestBody.
    // getBindingResult() contiene todos los errores de todos los campos que fallaron,
    // lo que nos permite devolver una lista completa en lugar de solo el primero.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {

        // Recorremos todos los errores de campo y extraemos el mensaje de cada anotación
        // (@NotBlank, @Min, etc.) que haya fallado. Puede haber más de uno.
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "La petición contiene campos inválidos",
                errors,
                LocalDateTime.now(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

}
