package com.fitness.workout.repository;

import com.fitness.workout.entity.RoutineEntity;
import com.fitness.workout.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para acceder a las rutinas en la BD.
 *
 * JpaRepository ya proporciona CRUD completo (save, findById, findAll, delete...).
 * Los métodos extra usan "derived queries": Spring Data genera el SQL automáticamente
 * a partir del nombre del método (ej. findByDayOfWeek → SELECT ... WHERE day_of_week = ?).
 */
@Repository
public interface RoutineRepository extends JpaRepository<RoutineEntity, Long> {

    // Buscar rutinas asignadas a un día concreto de la semana
    List<RoutineEntity> findByDayOfWeek(DayOfWeek dayOfWeek);

    // Buscar una rutina por nombre exacto (útil para evitar duplicados)
    Optional<RoutineEntity> findByName(String name);

    Optional<RoutineEntity> findByProgramIdAndDayOfWeek(Long programId, DayOfWeek dayOfWeek);

    List<RoutineEntity> findByProgramId(Long programId);
}
