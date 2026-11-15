package com.fitness.workout.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "logged_exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoggedExerciseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_session_id", referencedColumnName = "id", nullable = false)
    private WorkoutSessionEntity workoutSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_template_id", referencedColumnName = "id", nullable = false)
    private ExerciseTemplateEntity exerciseTemplate;

    @OneToMany(mappedBy = "loggedExercise", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numberSet ASC")
    @Builder.Default
    private List<LoggedSetEntity> sets = new ArrayList<>();

    @Column(nullable = false)
    private Integer orderInSession;

    @Column
    private String notes;
}
