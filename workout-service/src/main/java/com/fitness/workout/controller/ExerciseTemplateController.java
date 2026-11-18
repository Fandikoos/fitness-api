package com.fitness.workout.controller;

import com.fitness.workout.config.ApiConfig;
import com.fitness.workout.dto.ExerciseTemplateRequest;
import com.fitness.workout.dto.ExerciseTemplateResponse;
import com.fitness.workout.dto.PageResponse;
import com.fitness.workout.enums.MuscleGroup;
import com.fitness.workout.service.ExerciseTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Exercise Templates", description = "Catálogo de ejercicios disponibles para asignar a rutinas")
@RestController
@RequestMapping(ApiConfig.API_BASE_URL + "exercise-templates")
public class ExerciseTemplateController {

    private final ExerciseTemplateService exerciseTemplateService;

    public ExerciseTemplateController(ExerciseTemplateService exerciseTemplateService) {
        this.exerciseTemplateService = exerciseTemplateService;
    }

    @Operation(summary = "Crear un nuevo ejercicio en el catálogo")
    @ApiResponse(responseCode = "201", description = "Ejercicio creado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @PostMapping
    public ResponseEntity<ExerciseTemplateResponse> create(@Valid @RequestBody ExerciseTemplateRequest request) {
        ExerciseTemplateResponse response = exerciseTemplateService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Obtener ejercicio por ID")
    @ApiResponse(responseCode = "200", description = "Ejercicio encontrado")
    @ApiResponse(responseCode = "404", description = "Ejercicio no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<ExerciseTemplateResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(exerciseTemplateService.findById(id));
    }

    @Operation(summary = "Listar todos los ejercicios del catálogo")
    @ApiResponse(responseCode = "200", description = "Lista de ejercicios")
    @GetMapping
    public ResponseEntity<List<ExerciseTemplateResponse>> findAll() {
        return ResponseEntity.ok(exerciseTemplateService.findAll());
    }

    @Operation(summary = "Listar ejercicios por grupo muscular (paginado)")
    @ApiResponse(responseCode = "200", description = "Página de ejercicios del grupo muscular indicado")
    @GetMapping("/muscle-group/{muscleGroup}")
    public ResponseEntity<PageResponse<ExerciseTemplateResponse>> findByMuscleGroup(
            @PageableDefault(sort = "name") Pageable pageable, @PathVariable MuscleGroup muscleGroup) {
        return ResponseEntity.ok(exerciseTemplateService.findByMuscleGroup(muscleGroup, pageable));
    }

    @Operation(summary = "Actualizar un ejercicio del catálogo")
    @ApiResponse(responseCode = "200", description = "Ejercicio actualizado")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @ApiResponse(responseCode = "404", description = "Ejercicio no encontrado")
    @PutMapping("/{id}")
    public ResponseEntity<ExerciseTemplateResponse> update(@PathVariable Long id,
                                                           @Valid @RequestBody ExerciseTemplateRequest request) {
        return ResponseEntity.ok(exerciseTemplateService.update(id, request));
    }

    @Operation(summary = "Eliminar un ejercicio del catálogo")
    @ApiResponse(responseCode = "204", description = "Ejercicio eliminado")
    @ApiResponse(responseCode = "404", description = "Ejercicio no encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        exerciseTemplateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
