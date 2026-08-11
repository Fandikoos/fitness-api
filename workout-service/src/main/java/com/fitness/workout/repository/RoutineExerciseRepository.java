package com.fitness.workout.repository;

import com.fitness.workout.entity.RoutineExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para acceder a los ejercicios vinculados a una rutina.
 *
 * Los ejercicios en rutina siempre pertenecen a una rutina, así que el query
 * más común será buscar todos los de una rutina concreta, ordenados por posición.
 */
@Repository
public interface RoutineExerciseRepository extends JpaRepository<RoutineExerciseEntity, Long> {

    // Buscar todos los ejercicios de una rutina, ordenados por su posición
    List<RoutineExerciseEntity> findByRoutineIdOrderByOrderInRoutineAsc(Long routineId);
}
