package com.fitness.workout.service;

import com.fitness.workout.dto.RoutineRequest;
import com.fitness.workout.dto.RoutineResponse;
import com.fitness.workout.enums.DayOfWeek;

import java.util.List;

/**
 * Contrato para las operaciones de negocio sobre rutinas.
 *
 * Usar una interfaz permite desacoplar el controller de la implementación concreta.
 * Esto facilita testear con mocks y cambiar la implementación sin tocar el controller.
 */
public interface RoutineService {

    RoutineResponse create(RoutineRequest request);

    RoutineResponse findById(Long id);

    List<RoutineResponse> findAll();

    List<RoutineResponse> findByDayOfWeek(DayOfWeek dayOfWeek);

    RoutineResponse update(Long id, RoutineRequest request);

    void delete(Long id);
}
