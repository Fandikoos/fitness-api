package com.fitness.workout.controller;

import com.fitness.workout.config.ApiConfig;
import com.fitness.workout.dto.RoutineRequest;
import com.fitness.workout.dto.RoutineResponse;
import com.fitness.workout.enums.DayOfWeek;
import com.fitness.workout.service.RoutineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestionar rutinas de entrenamiento.
 *
 * @RestController combina @Controller + @ResponseBody: todas las respuestas
 * se serializan automáticamente a JSON (no devuelve vistas HTML).
 *
 * @RequestMapping define el prefijo de URL para todos los endpoints de este controller.
 */
@RestController
@RequestMapping(ApiConfig.API_BASE_URL + "routines")
public class RoutineController {

    private final RoutineService routineService;

    public RoutineController(RoutineService routineService) {
        this.routineService = routineService;
    }

    // @Valid activa la validación de las anotaciones del DTO (@NotBlank, @Min, etc.).
    // Si alguna validación falla, Spring devuelve automáticamente un 400 Bad Request.
    @PostMapping
    public ResponseEntity<RoutineResponse> create(@Valid @RequestBody RoutineRequest request) {
        RoutineResponse response = routineService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoutineResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(routineService.findById(id));
    }

    // @RequestParam(required = false) hace que el parámetro sea opcional.
    // Si se pasa ?dayOfWeek=MONDAY, filtra por día; si no, devuelve todas.
    @GetMapping
    public ResponseEntity<List<RoutineResponse>> findAll(
            @RequestParam(required = false) DayOfWeek dayOfWeek) {
        List<RoutineResponse> routines = dayOfWeek != null
                ? routineService.findByDayOfWeek(dayOfWeek)
                : routineService.findAll();
        return ResponseEntity.ok(routines);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoutineResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody RoutineRequest request) {
        return ResponseEntity.ok(routineService.update(id, request));
    }

    // 204 No Content es el código HTTP estándar para operaciones de borrado exitosas
    // donde no hay cuerpo de respuesta que devolver.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
