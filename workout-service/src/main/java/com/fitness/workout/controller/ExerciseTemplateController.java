package com.fitness.workout.controller;

import com.fitness.workout.config.ApiConfig;
import com.fitness.workout.dto.ExerciseTemplateRequest;
import com.fitness.workout.dto.ExerciseTemplateResponse;
import com.fitness.workout.enums.MuscleGroup;
import com.fitness.workout.service.ExerciseTemplateService;
import jakarta.validation.Valid;
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

    // @RequestParam(required = false) hace que el parámetro sea opcional.
    // Si se pasa ?muscleGroup=CHEST, filtra por grupo muscular; si no, devuelve todos.
    @GetMapping
    public ResponseEntity<List<ExerciseTemplateResponse>> findAll(
            @RequestParam(required = false) MuscleGroup muscleGroup) {
        List<ExerciseTemplateResponse> templates = muscleGroup != null
                ? exerciseTemplateService.findByMuscleGroup(muscleGroup)
                : exerciseTemplateService.findAll();
        return ResponseEntity.ok(templates);
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
