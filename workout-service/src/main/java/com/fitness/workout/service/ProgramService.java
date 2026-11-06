package com.fitness.workout.service;

import com.fitness.workout.dto.ProgramRequest;
import com.fitness.workout.dto.ProgramResponse;

import java.util.List;

/**
 * Contrato para las operaciones de negocio sobre programas de entrenamiento.
 *
 * Usar una interfaz permite desacoplar el controller de la implementación concreta.
 * Esto facilita testear con mocks y cambiar la implementación sin tocar el controller.
 */
public interface ProgramService {

    ProgramResponse create(ProgramRequest request);

    ProgramResponse findById(Long id);

    List<ProgramResponse> findAll();

    ProgramResponse update(Long id, ProgramRequest request);

    ProgramResponse findActive();

    void delete(Long id);
}
