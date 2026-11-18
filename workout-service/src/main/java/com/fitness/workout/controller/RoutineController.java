package com.fitness.workout.controller;

import com.fitness.workout.config.ApiConfig;
import com.fitness.workout.dto.RoutineRequest;
import com.fitness.workout.dto.RoutineResponse;
import com.fitness.workout.enums.DayOfWeek;
import com.fitness.workout.service.RoutineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestionar rutinas de entrenamiento.
 *
 * @RestController combina @Controller + @ResponseBody: todas las respuestas
 * se serializan automáticamente a JSON (no devuelve vistas HTML).
 *
 * @RequestMapping define el prefijo de URL para todos los endpoints de este controller.
 */
@Tag(name = "Routines", description = "Gestión de rutinas de entrenamiento")
@RestController
@RequestMapping(ApiConfig.API_BASE_URL + "routines")
public class RoutineController {

    private final RoutineService routineService;

    public RoutineController(RoutineService routineService) {
        this.routineService = routineService;
    }

    // @Valid activa la validación de las anotaciones del DTO (@NotBlank, @Min, etc.).
    // Si alguna validación falla, Spring devuelve automáticamente un 400 Bad Request.
    @Operation(summary = "Crear una nueva rutina de entrenamiento")
    @ApiResponse(responseCode = "201", description = "Rutina creada correctamente")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @PostMapping
    public ResponseEntity<RoutineResponse> create(@Valid @RequestBody RoutineRequest request) {
        RoutineResponse response = routineService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Obtener rutina por ID")
    @ApiResponse(responseCode = "200", description = "Rutina encontrada")
    @ApiResponse(responseCode = "404", description = "Rutina no encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<RoutineResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(routineService.findById(id));
    }

    @Operation(summary = "Listar rutinas de un programa")
    @ApiResponse(responseCode = "200", description = "Lista de rutinas del programa")
    @ApiResponse(responseCode = "404", description = "Programa no encontrado")
    @GetMapping("/program/{programId}")
    public ResponseEntity<List<RoutineResponse>> findByProgramId(@PathVariable Long programId) {
        List<RoutineResponse> routines = routineService.findByProgramId(programId);
        return ResponseEntity.ok(routines);
    }

    // @RequestParam(required = false) hace que el parámetro sea opcional.
    // Si se pasa ?dayOfWeek=MONDAY, filtra por día; si no, devuelve todas.
    @Operation(summary = "Listar rutinas", description = "Devuelve todas las rutinas. Si se indica dayOfWeek, filtra por ese día.")
    @ApiResponse(responseCode = "200", description = "Lista de rutinas")
    @GetMapping
    public ResponseEntity<List<RoutineResponse>> findAll(
            @RequestParam(required = false) DayOfWeek dayOfWeek) {
        List<RoutineResponse> routines = dayOfWeek != null
                ? routineService.findByDayOfWeek(dayOfWeek)
                : routineService.findAll();
        return ResponseEntity.ok(routines);
    }

    @Operation(summary = "Actualizar una rutina existente")
    @ApiResponse(responseCode = "200", description = "Rutina actualizada")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @ApiResponse(responseCode = "404", description = "Rutina no encontrada")
    @PutMapping("/{id}")
    public ResponseEntity<RoutineResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody RoutineRequest request) {
        return ResponseEntity.ok(routineService.update(id, request));
    }

    // 204 No Content es el código HTTP estándar para operaciones de borrado exitosas
    // donde no hay cuerpo de respuesta que devolver.
    @Operation(summary = "Eliminar una rutina")
    @ApiResponse(responseCode = "204", description = "Rutina eliminada")
    @ApiResponse(responseCode = "404", description = "Rutina no encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
