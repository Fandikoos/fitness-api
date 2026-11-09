package com.fitness.workout.service;

import com.fitness.workout.dto.WorkoutSessionRequest;
import com.fitness.workout.dto.WorkoutSessionResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkoutSessionService {

    WorkoutSessionResponse createWorkoutSession(WorkoutSessionRequest workoutSessionRequest);

    List<WorkoutSessionResponse> getBySessionDate(LocalDate sessionDateFrom, LocalDate sessionDateTo);

    WorkoutSessionResponse getById(Long id);

    // Optional vacío → el controller devuelve 204 (igual que /programs/active)
    Optional<WorkoutSessionResponse> getLatestWorkoutSessionByRoutineId(Long routineId);

    void delete(Long id);
}
