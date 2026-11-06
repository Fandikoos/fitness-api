package com.fitness.workout.repository;

import com.fitness.workout.entity.WorkoutSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSessionEntity, Long> {

    List<WorkoutSessionEntity> findBySessionDateBetweenOrderBySessionDateDesc(LocalDate startDate, LocalDate endDate);

    Optional<WorkoutSessionEntity> findFirstByRoutineIdOrderBySessionDateDesc(Long routineId);
}
