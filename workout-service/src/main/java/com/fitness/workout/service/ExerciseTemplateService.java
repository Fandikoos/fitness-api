package com.fitness.workout.service;

import com.fitness.workout.dto.ExerciseTemplateRequest;
import com.fitness.workout.dto.ExerciseTemplateResponse;
import com.fitness.workout.enums.MuscleGroup;

import java.util.List;

/**
 * Contrato para operaciones sobre el catálogo de ejercicios.
 *
 * El catálogo es independiente de las rutinas: contiene todos los ejercicios
 * disponibles que el usuario puede seleccionar al configurar sus rutinas.
 */
public interface ExerciseTemplateService {

    ExerciseTemplateResponse create(ExerciseTemplateRequest request);

    ExerciseTemplateResponse findById(Long id);

    List<ExerciseTemplateResponse> findAll();

    List<ExerciseTemplateResponse> findByMuscleGroup(MuscleGroup muscleGroup);

    ExerciseTemplateResponse update(Long id, ExerciseTemplateRequest request);

    void delete(Long id);
}
