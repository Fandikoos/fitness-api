package com.fitness.workout.service;

import com.fitness.workout.dto.RoutineExerciseRequest;
import com.fitness.workout.dto.RoutineExerciseResponse;

import java.util.List;

/**
 * Contrato para operaciones sobre ejercicios vinculados a una rutina.
 *
 * Los ejercicios en rutina siempre existen en el contexto de una rutina (routineId),
 * y hacen referencia a un ejercicio del catálogo (exerciseTemplateId).
 */
public interface RoutineExerciseService {

    RoutineExerciseResponse addToRoutine(Long routineId, RoutineExerciseRequest request);

    List<RoutineExerciseResponse> findByRoutineId(Long routineId);

    RoutineExerciseResponse update(Long routineId, Long routineExerciseId, RoutineExerciseRequest request);

    void delete(Long routineId, Long routineExerciseId);
}
