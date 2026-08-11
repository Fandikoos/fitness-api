package com.fitness.workout.repository;

import com.fitness.workout.entity.ExerciseTemplateEntity;
import com.fitness.workout.enums.MuscleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para acceder al catálogo de ejercicios en la BD.
 *
 * Permite buscar ejercicios por grupo muscular, lo que es útil para
 * que el frontend muestre ejercicios filtrados (ej. "ejercicios de pecho").
 */
@Repository
public interface ExerciseTemplateRepository extends JpaRepository<ExerciseTemplateEntity, Long> {

    // Buscar ejercicios del catálogo por grupo muscular
    List<ExerciseTemplateEntity> findByMuscleGroup(MuscleGroup muscleGroup);
}
