package com.fitness.workout.controller;

import com.fitness.workout.config.ApiConfig;
import com.fitness.workout.dto.ExerciseTemplateRequest;
import com.fitness.workout.dto.ExerciseTemplateResponse;
import com.fitness.workout.dto.PageResponse;
import com.fitness.workout.enums.MuscleGroup;
import com.fitness.workout.service.ExerciseTemplateService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestionar el catálogo de ejercicios.
 *
 * Estos endpoints permiten al frontend listar, crear y gestionar
 * los ejercicios disponibles (plantillas) antes de asignarlos a rutinas.
 * Son independientes de cualquier rutina concreta.
 */
@RestController
@RequestMapping(ApiConfig.API_BASE_URL + "exercise-templates")
public class ExerciseTemplateController {

    private final ExerciseTemplateService exerciseTemplateService;

    public ExerciseTemplateController(ExerciseTemplateService exerciseTemplateService) {
        this.exerciseTemplateService = exerciseTemplateService;
    }

    @PostMapping
    public ResponseEntity<ExerciseTemplateResponse> create(@Valid @RequestBody ExerciseTemplateRequest request) {
        ExerciseTemplateResponse response = exerciseTemplateService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseTemplateResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(exerciseTemplateService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<ExerciseTemplateResponse>> findAll() {
        return ResponseEntity.ok(exerciseTemplateService.findAll());
    }

    @GetMapping("/muscle-group/{muscleGroup}")
    public ResponseEntity<PageResponse<ExerciseTemplateResponse>> findByMuscleGroup(
            @PageableDefault(sort = "name")Pageable pageable, @PathVariable MuscleGroup muscleGroup) {
        return ResponseEntity.ok(exerciseTemplateService.findByMuscleGroup(muscleGroup, pageable));
    }

                                                          @PutMapping("/{id}")
    public ResponseEntity<ExerciseTemplateResponse> update(@PathVariable Long id,
                                                          @Valid @RequestBody ExerciseTemplateRequest request) {
        return ResponseEntity.ok(exerciseTemplateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        exerciseTemplateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
