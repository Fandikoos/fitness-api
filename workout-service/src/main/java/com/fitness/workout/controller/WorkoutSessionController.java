package com.fitness.workout.controller;

import com.fitness.workout.config.ApiConfig;
import com.fitness.workout.dto.WorkoutSessionRequest;
import com.fitness.workout.dto.WorkoutSessionResponse;
import com.fitness.workout.service.WorkoutSessionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(ApiConfig.API_BASE_URL + "workout-sessions")
@AllArgsConstructor
public class WorkoutSessionController {

    private final WorkoutSessionService workoutSessionService;

    @PostMapping
    public ResponseEntity<WorkoutSessionResponse> create(@Valid @RequestBody WorkoutSessionRequest request) {
        WorkoutSessionResponse response = workoutSessionService.createWorkoutSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/between-dates")
    public ResponseEntity<List<WorkoutSessionResponse>> findBetweenDates(
            // @DateTimeFormat garantiza que Spring parsee fechas ISO-8601 (yyyy-MM-dd).
            // Sin él, una fecha mal formada puede producir un 500 en vez del 400 que esperamos.
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<WorkoutSessionResponse> response = workoutSessionService.getBySessionDate(start, end);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutSessionResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(workoutSessionService.getById(id));
    }

    @GetMapping("/routines/{routineId}/latest")
    public ResponseEntity<WorkoutSessionResponse> findLatest(@PathVariable Long routineId) {
        // Optional vacío → 204 (igual que /programs/active): el front sabe que no hay historial
        return workoutSessionService.getLatestWorkoutSessionByRoutineId(routineId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        workoutSessionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
