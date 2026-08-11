package com.fitness.workout.entity;

import com.fitness.workout.enums.MuscleGroup;
import jakarta.persistence.*;
import lombok.*;

/**
 * Representa un ejercicio en el catálogo general (ej. "Press Banca", "Sentadilla").
 *
 * Esta entidad es independiente de cualquier rutina: define QUÉ es el ejercicio
 * (nombre, grupo muscular, imágenes). Es como una "plantilla" que luego se
 * vincula a rutinas concretas a través de RoutineExerciseEntity.
 *
 * Separar catálogo de uso en rutina permite:
 * - Listar todos los ejercicios disponibles para que el usuario elija
 * - Reutilizar el mismo ejercicio en varias rutinas
 * - Actualizar imagen/vídeo en un solo lugar y que se refleje en todas las rutinas
 */
@Entity
@Table(name = "exercise_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del ejercicio (ej. "Sentadilla", "Press Militar")
    @Column(nullable = false)
    private String name;

    // Grupo muscular principal que trabaja este ejercicio (ej. CHEST, BACK, BICEPS).
    // @Enumerated(STRING) guarda el nombre del enum como texto en la BD,
    // igual que hacemos con DayOfWeek en RoutineEntity.
    @Enumerated(EnumType.STRING)
    @Column
    private MuscleGroup muscleGroup;

    // URL de una imagen ilustrativa del ejercicio. Nullable porque no siempre
    // se dispone de imagen. Se guarda como TEXT en vez de VARCHAR(255) porque
    // las URLs pueden ser largas (ej. URLs firmadas de S3 con tokens).
    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    // URL de un vídeo demostrativo de cómo ejecutar el ejercicio correctamente.
    // Mismo criterio que imageUrl: solo almacenamos la URL, el archivo real
    // se almacena en un servicio externo (S3, MinIO, etc.), nunca en la BD.
    @Column(columnDefinition = "TEXT")
    private String videoUrl;
}
