package com.fitness.workout.controller;

import com.fitness.workout.config.ApiConfig;
import com.fitness.workout.dto.ProgramRequest;
import com.fitness.workout.dto.ProgramResponse;
import com.fitness.workout.service.ProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestionar programas de entrenamiento.
 *
 * @RestController combina @Controller + @ResponseBody: todas las respuestas
 * se serializan automáticamente a JSON (no devuelve vistas HTML).
 *
 * @RequestMapping define el prefijo de URL para todos los endpoints de este controller.
 */
@Tag(name = "Programs", description = "Gestión de programas de entrenamiento")
@RestController
@RequestMapping(ApiConfig.API_BASE_URL + "programs")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    // @Valid activa la validación de las anotaciones del DTO (@NotBlank, etc.).
    // Si alguna validación falla, Spring devuelve automáticamente un 400 Bad Request.
    @Operation(summary = "Crear un nuevo programa de entrenamiento")
    @ApiResponse(responseCode = "201", description = "Programa creado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @PostMapping
    public ResponseEntity<ProgramResponse> create(@Valid @RequestBody ProgramRequest request) {
        ProgramResponse response = programService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Obtener programa por ID")
    @ApiResponse(responseCode = "200", description = "Programa encontrado")
    @ApiResponse(responseCode = "404", description = "Programa no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<ProgramResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(programService.findById(id));
    }

    @Operation(summary = "Obtener el programa activo", description = "Solo puede haber un programa activo a la vez")
    @ApiResponse(responseCode = "200", description = "Programa activo encontrado")
    @ApiResponse(responseCode = "404", description = "No hay ningún programa activo")
    @GetMapping("/active")
    public ResponseEntity<ProgramResponse> findActive() {
        return ResponseEntity.ok(programService.findActive());
    }

    @Operation(summary = "Listar todos los programas")
    @ApiResponse(responseCode = "200", description = "Lista de programas")
    @GetMapping
    public ResponseEntity<List<ProgramResponse>> findAll() {
        return ResponseEntity.ok(programService.findAll());
    }

    @Operation(summary = "Actualizar un programa existente")
    @ApiResponse(responseCode = "200", description = "Programa actualizado")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @ApiResponse(responseCode = "404", description = "Programa no encontrado")
    @PutMapping("/{id}")
    public ResponseEntity<ProgramResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody ProgramRequest request) {
        return ResponseEntity.ok(programService.update(id, request));
    }

    // 204 No Content es el código HTTP estándar para operaciones de borrado exitosas
    // donde no hay cuerpo de respuesta que devolver.
    @Operation(summary = "Eliminar un programa")
    @ApiResponse(responseCode = "204", description = "Programa eliminado")
    @ApiResponse(responseCode = "404", description = "Programa no encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        programService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
