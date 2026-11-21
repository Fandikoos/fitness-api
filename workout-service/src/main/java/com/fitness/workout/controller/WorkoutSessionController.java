package com.fitness.workout.controller;

import com.fitness.workout.config.ApiConfig;
import com.fitness.workout.dto.WorkoutSessionRequest;
import com.fitness.workout.dto.WorkoutSessionResponse;
import com.fitness.workout.service.WorkoutSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Workout Sessions", description = "Registro de sesiones de entrenamiento completadas")
@RestController
@RequestMapping(ApiConfig.API_BASE_URL + "workout-sessions")
@AllArgsConstructor
public class WorkoutSessionController {

    private final WorkoutSessionService workoutSessionService;

    @Operation(summary = "Registrar una nueva sesión de entrenamiento")
    @ApiResponse(responseCode = "201", description = "Sesión registrada correctamente")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @PostMapping
    public ResponseEntity<WorkoutSessionResponse> create(@Valid @RequestBody WorkoutSessionRequest request) {
        WorkoutSessionResponse response = workoutSessionService.createWorkoutSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Listar sesiones entre dos fechas")
    @ApiResponse(responseCode = "200", description = "Lista de sesiones en el rango de fechas indicado")
    @GetMapping("/between-dates")
    public ResponseEntity<List<WorkoutSessionResponse>> findBetweenDates(
            // @DateTimeFormat garantiza que Spring parsee fechas ISO-8601 (yyyy-MM-dd).
            // Sin él, una fecha mal formada puede producir un 500 en vez del 400 que esperamos.
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<WorkoutSessionResponse> response = workoutSessionService.getBySessionDate(start, end);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener sesión por ID")
    @ApiResponse(responseCode = "200", description = "Sesión encontrada")
    @ApiResponse(responseCode = "404", description = "Sesión no encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<WorkoutSessionResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(workoutSessionService.getById(id));
    }

    @Operation(summary = "Obtener la última sesión de una rutina",
            description = "Devuelve 204 si la rutina aún no tiene sesiones registradas")
    @ApiResponse(responseCode = "200", description = "Última sesión encontrada")
    @ApiResponse(responseCode = "204", description = "Sin historial para esta rutina")
    @GetMapping("/routines/{routineId}/latest")
    public ResponseEntity<WorkoutSessionResponse> findLatest(@PathVariable Long routineId) {
        // Optional vacío → 204 (igual que /programs/active): el front sabe que no hay historial
        return workoutSessionService.getLatestWorkoutSessionByRoutineId(routineId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Eliminar una sesión de entrenamiento")
    @ApiResponse(responseCode = "204", description = "Sesión eliminada")
    @ApiResponse(responseCode = "404", description = "Sesión no encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        workoutSessionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
