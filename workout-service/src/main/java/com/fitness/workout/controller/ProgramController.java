package com.fitness.workout.controller;

import com.fitness.workout.config.ApiConfig;
import com.fitness.workout.dto.ProgramRequest;
import com.fitness.workout.dto.ProgramResponse;
import com.fitness.workout.service.ProgramService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestionar programas de entrenamiento.
 *
 * @RestController combina @Controller + @ResponseBody: todas las respuestas
 * se serializan automáticamente a JSON (no devuelve vistas HTML).
 *
 * @RequestMapping define el prefijo de URL para todos los endpoints de este controller.
 */
@RestController
@RequestMapping(ApiConfig.API_BASE_URL + "programs")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    // @Valid activa la validación de las anotaciones del DTO (@NotBlank, etc.).
    // Si alguna validación falla, Spring devuelve automáticamente un 400 Bad Request.
    @PostMapping
    public ResponseEntity<ProgramResponse> create(@Valid @RequestBody ProgramRequest request) {
        ProgramResponse response = programService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(programService.findById(id));
    }

    @GetMapping("/active")
    public ResponseEntity<ProgramResponse> findActive() {
        return ResponseEntity.ok(programService.findActive());
    }

    @GetMapping
    public ResponseEntity<List<ProgramResponse>> findAll() {
        return ResponseEntity.ok(programService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgramResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody ProgramRequest request) {
        return ResponseEntity.ok(programService.update(id, request));
    }

    // 204 No Content es el código HTTP estándar para operaciones de borrado exitosas
    // donde no hay cuerpo de respuesta que devolver.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        programService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
