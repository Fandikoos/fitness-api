package com.fitness.workout.config;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fitness.workout.dto.ErrorResponse;
import com.fitness.workout.exception.BusinessValidationException;
import com.fitness.workout.exception.ConflictException;
import com.fitness.workout.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// @RestControllerAdvice intercepta excepciones lanzadas en cualquier @RestController
// de la aplicación, centralizando el manejo de errores en un único lugar.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String badRequest = "Bad Request";
    private static final String conflict = "Conflict";
    private static final String notFound = "Not Found";
    private static final String unprocessableEntity = "Unprocessable Entity";
    private static final String internalServerError = "Internal Server Error";

    // WebRequest.getDescription(false) devuelve "uri=/api/routines/5" — eliminamos
    // el prefijo para que el campo path del ErrorResponse muestre solo la ruta limpia.
    private String extractPath(WebRequest request) {
        String description = request.getDescription(false);
        return description.startsWith("uri=") ? description.substring(4) : description;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                notFound,
                ex.getMessage(),
                null,
                Instant.now(),
                extractPath(request)
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
                conflict,
                ex.getMessage(),
                null,
                Instant.now(),
                extractPath(request)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                conflict,
                "La operación viola una restricción de integridad de datos",
                null,
                Instant.now(),
                extractPath(request)
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
                unprocessableEntity,
                ex.getMessage(),
                null,
                Instant.now(),
                extractPath(request)
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
                badRequest,
                "La petición contiene campos inválidos",
                errors,
                Instant.now(),
                extractPath(request)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Spring lanza MethodArgumentTypeMismatchException cuando un @PathVariable o
    // @RequestParam no puede convertirse al tipo Java esperado. El caso más común:
    // pasar "FOO" donde se espera un enum, o "abc" donde se espera un Long.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        String message;
        Class<?> requiredType = ex.getRequiredType();

        // Si el tipo esperado es un enum, listamos sus constantes para orientar al cliente.
        if (requiredType != null && requiredType.isEnum()) {
            String validValues = Arrays.stream(requiredType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            message = "El valor '%s' no es válido para el parámetro '%s'. Valores aceptados: %s"
                    .formatted(ex.getValue(), ex.getName(), validValues);
        } else {
            message = "El parámetro '%s' debe ser de tipo %s pero se recibió: '%s'"
                    .formatted(
                            ex.getName(),
                            requiredType != null ? requiredType.getSimpleName() : "desconocido",
                            ex.getValue()
                    );
        }

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                badRequest,
                message,
                null,
                Instant.now(),
                extractPath(request)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Spring lanza HttpMessageNotReadableException cuando el body JSON está mal formado
    // o Jackson no puede deserializar un campo (ej: dayOfWeek: "Monday" en lugar de "MONDAY").
    // Inspeccionamos la causa para dar un mensaje más preciso cuando el problema es un enum.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {

        String message = "El cuerpo de la petición no es válido o tiene formato JSON incorrecto";

        // InvalidFormatException es la excepción de Jackson para valores que no encajan
        // con el tipo destino. Si el tipo es un enum, listamos los valores válidos.
        if (ex.getCause() instanceof InvalidFormatException ife
                && ife.getTargetType() != null
                && ife.getTargetType().isEnum()) {
            String validValues = Arrays.stream(ife.getTargetType().getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            message = "El valor '%s' no es válido. Valores aceptados: %s"
                    .formatted(ife.getValue(), validValues);
        }

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                badRequest,
                message,
                null,
                Instant.now(),
                extractPath(request)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handler de último recurso: captura cualquier excepción no controlada por los
    // handlers anteriores. Loguea el stacktrace completo en el servidor (para diagnóstico)
    // pero NO lo expone en la respuesta HTTP, evitando filtrar detalles internos.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception ex, WebRequest request) {

        log.error("Error inesperado en {}: {}", extractPath(request), ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                internalServerError,
                "Ha ocurrido un error inesperado. Consulta los logs del servidor para más detalle.",
                null,
                Instant.now(),
                extractPath(request)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
