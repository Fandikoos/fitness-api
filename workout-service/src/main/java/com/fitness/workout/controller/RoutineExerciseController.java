package com.fitness.workout.controller;

import com.fitness.workout.config.ApiConfig;
import com.fitness.workout.dto.RoutineExerciseRequest;
import com.fitness.workout.dto.RoutineExerciseResponse;
import com.fitness.workout.service.RoutineExerciseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestionar ejercicios dentro de una rutina.
 *
 * Los endpoints están anidados bajo /api/routines/{routineId}/exercises
 * porque un ejercicio en rutina siempre pertenece a una rutina (recurso anidado REST).
 * Cada ejercicio referencia un template del catálogo mediante exerciseTemplateId.
 */
@RestController
@RequestMapping(ApiConfig.API_BASE_URL + "routines/{routineId}/exercises")
public class RoutineExerciseController {

    private final RoutineExerciseService routineExerciseService;

    public RoutineExerciseController(RoutineExerciseService routineExerciseService) {
        this.routineExerciseService = routineExerciseService;
    }

    @PostMapping
    public ResponseEntity<RoutineExerciseResponse> addToRoutine(@PathVariable Long routineId,
                                                                 @Valid @RequestBody RoutineExerciseRequest request) {
        RoutineExerciseResponse response = routineExerciseService.addToRoutine(routineId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RoutineExerciseResponse>> findByRoutineId(@PathVariable Long routineId) {
        return ResponseEntity.ok(routineExerciseService.findByRoutineId(routineId));
    }

    @PutMapping("/{exerciseId}")
    public ResponseEntity<RoutineExerciseResponse> update(@PathVariable Long routineId,
                                                           @PathVariable Long exerciseId,
                                                           @Valid @RequestBody RoutineExerciseRequest request) {
        return ResponseEntity.ok(routineExerciseService.update(routineId, exerciseId, request));
    }

    @DeleteMapping("/{exerciseId}")
    public ResponseEntity<Void> delete(@PathVariable Long routineId,
                                       @PathVariable Long exerciseId) {
        routineExerciseService.delete(routineId, exerciseId);
        return ResponseEntity.noContent().build();
    }
}
