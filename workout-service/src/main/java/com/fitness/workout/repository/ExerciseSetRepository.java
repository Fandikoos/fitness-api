package com.fitness.workout.repository;

import com.fitness.workout.entity.ExerciseSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para acceder a las series de ejercicios en la BD.
 *
 * Aunque normalmente las series se cargan a través de RoutineExerciseEntity.sets,
 * tener repositorio propio permite hacer queries directas cuando sea necesario.
 */
@Repository
public interface ExerciseSetRepository extends JpaRepository<ExerciseSetEntity, Long> {

    // Buscar todas las series de un ejercicio en rutina, ordenadas por número de serie
    List<ExerciseSetEntity> findByRoutineExerciseIdOrderByNumberSetAsc(Long routineExerciseId);
}
