package com.fitness.workout.enums;

import java.util.List;

/**
 * Grupo muscular principal que trabaja un ejercicio.
 *
 * Se usa como enum (en vez de String libre) para evitar inconsistencias
 * en los datos (ej. "pecho", "Pecho", "PECHO") y facilitar filtros/agrupaciones
 * en el frontend y en futuras queries.
 */
public enum MuscleGroup {
    CHEST,        // Pecho
    BACK,         // Espalda
    SHOULDERS,    // Hombros
    BICEPS,       // Bíceps
    TRICEPS,      // Tríceps
    QUADRICEPS,   // Cuádriceps
    HAMSTRINGS,   // Isquiotibiales
    GLUTES,       // Glúteos
    CALVES,       // Gemelos
    ABS,          // Abdominales
    FOREARMS,     // Antebrazos
    TRAPS         // Trapecios
    ;

    private static final List<MuscleGroup> validMuscleGroups = List.of(MuscleGroup.values());

    public static boolean isValid(MuscleGroup muscleGroup) {
        return validMuscleGroups.contains(muscleGroup);
    }
}
