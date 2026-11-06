package com.fitness.workout.repository;

import com.fitness.workout.TestcontainersConfiguration;
import com.fitness.workout.entity.*;
import com.fitness.workout.enums.MuscleGroup;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de integración para WorkoutSessionRepository.
 *
 * --- Por qué @DataJpaTest en lugar de @SpringBootTest ---
 * @DataJpaTest es un "slice test": carga solo la capa JPA (entidades, repositorios,
 * DataSource, Flyway) sin levantar el servidor web ni los servicios. Es más rápido
 * que @SpringBootTest y más apropiado cuando solo quieres probar consultas a base
 * de datos.
 *
 * --- Por qué @AutoConfigureTestDatabase(replace = NONE) ---
 * Por defecto @DataJpaTest sustituye el DataSource por una base de datos embebida
 * en memoria (H2). Eso nos impediría probar cosas específicas de MySQL como el
 * UNIQUE(routine_id, session_date) definido en el SQL de Flyway o el ON DELETE SET NULL.
 * Con replace = NONE le decimos que use el DataSource real: el MySQL del Testcontainer.
 *
 * --- Por qué @Import(TestcontainersConfiguration.class) ---
 * TestcontainersConfiguration arranca un contenedor Docker con MySQL y lo registra
 * como @ServiceConnection: Spring Boot auto-configura el DataSource apuntando a ese
 * contenedor. Sin este @Import, @DataJpaTest no sabe que existe ese bean.
 *
 * --- Transacciones en @DataJpaTest ---
 * Cada @Test se ejecuta dentro de una transacción que se hace rollback al finalizar.
 * Eso garantiza que los datos de un test no contaminen los siguientes.
 * Cuando necesitamos que un INSERT llegue realmente a MySQL (para disparar un
 * constraint), usamos saveAndFlush() — que hace flush de la sesión de Hibernate
 * antes de que termine el método, enviando el SQL al motor aunque la transacción
 * aún no haya hecho commit.
 */
@DataJpaTest
// Por defecto @DataJpaTest sustituye el DataSource por H2. Con esta propiedad
// le decimos que use el MySQL real del Testcontainer, necesario para probar
// constraints de Flyway (UNIQUE, ON DELETE SET NULL) específicos de MySQL.
@TestPropertySource(properties = "spring.test.database.replace=none")
@Import(TestcontainersConfiguration.class)
class WorkoutSessionRepositoryTest {

    @Autowired
    private WorkoutSessionRepository sessionRepository;

    @Autowired
    private RoutineRepository routineRepository;

    @Autowired
    private ExerciseTemplateRepository exerciseTemplateRepository;

    /**
     * EntityManager da acceso directo a la sesión de Hibernate.
     * Lo usamos para llamar a clear() y vaciar la caché de primer nivel (L1),
     * forzando que los finds posteriores vayan a la BD en lugar de al cache.
     */
    @Autowired
    private EntityManager entityManager;

    // -------------------------------------------------------------------------
    // Helpers para construir fixtures de test sin repetir boilerplate
    // -------------------------------------------------------------------------

    private RoutineEntity persistRoutine(String name) {
        return routineRepository.save(
                RoutineEntity.builder()
                        .name(name)
                        .build()
        );
    }

    private ExerciseTemplateEntity persistTemplate() {
        return exerciseTemplateRepository.save(
                ExerciseTemplateEntity.builder()
                        .name("Press Banca")
                        .muscleGroup(MuscleGroup.CHEST)
                        .build()
        );
    }

    private WorkoutSessionEntity buildSession(LocalDate date, RoutineEntity routine) {
        return WorkoutSessionEntity.builder()
                .sessionDate(date)
                .routine(routine)
                // routineName es un snapshot: se guarda en el momento del entrenamiento
                // para que el historial no se pierda si la rutina se renombra o se borra.
                .routineName(routine != null ? routine.getName() : "Rutina eliminada")
                .build();
    }

    // =========================================================================
    // findBySessionDateBetweenOrderBySessionDateDesc
    // =========================================================================

    @Test
    void should_return_sessions_ordered_by_date_desc_when_querying_by_date_range() {
        RoutineEntity routine = persistRoutine("Push Day");

        // Tres sesiones: dos dentro del rango, una fuera
        sessionRepository.save(buildSession(LocalDate.of(2026, 11, 1), routine));
        sessionRepository.save(buildSession(LocalDate.of(2026, 11, 3), routine));
        sessionRepository.save(buildSession(LocalDate.of(2026, 10, 25), routine)); // fuera del rango
        entityManager.flush();

        List<WorkoutSessionEntity> result = sessionRepository
                .findBySessionDateBetweenOrderBySessionDateDesc(
                        LocalDate.of(2026, 11, 1),
                        LocalDate.of(2026, 11, 7)
                );

        assertThat(result).hasSize(2);
        // Verificamos el orden DESC: la más reciente primero
        assertThat(result.get(0).getSessionDate()).isEqualTo(LocalDate.of(2026, 11, 3));
        assertThat(result.get(1).getSessionDate()).isEqualTo(LocalDate.of(2026, 11, 1));
    }

    @Test
    void should_return_empty_list_when_no_sessions_exist_in_date_range() {
        // No guardamos ninguna sesión; el repositorio debe devolver lista vacía
        List<WorkoutSessionEntity> result = sessionRepository
                .findBySessionDateBetweenOrderBySessionDateDesc(
                        LocalDate.of(2026, 11, 1),
                        LocalDate.of(2026, 11, 7)
                );

        assertThat(result).isEmpty();
    }

    // =========================================================================
    // findFirstByRoutineIdOrderBySessionDateDesc
    // =========================================================================

    @Test
    void should_return_most_recent_session_when_routine_has_multiple_sessions() {
        RoutineEntity routine = persistRoutine("Pull Day");

        // Dos sesiones para la misma rutina en fechas distintas
        sessionRepository.save(buildSession(LocalDate.of(2026, 10, 28), routine));
        sessionRepository.save(buildSession(LocalDate.of(2026, 11, 4), routine));
        entityManager.flush();

        Optional<WorkoutSessionEntity> result = sessionRepository
                .findFirstByRoutineIdOrderBySessionDateDesc(routine.getId());

        // findFirst con ORDER BY DESC devuelve la más reciente
        assertThat(result).isPresent();
        assertThat(result.get().getSessionDate()).isEqualTo(LocalDate.of(2026, 11, 4));
    }

    @Test
    void should_return_empty_when_routine_has_no_sessions() {
        RoutineEntity routine = persistRoutine("Legs");

        Optional<WorkoutSessionEntity> result = sessionRepository
                .findFirstByRoutineIdOrderBySessionDateDesc(routine.getId());

        assertThat(result).isEmpty();
    }

    // =========================================================================
    // Constraint UNIQUE(routine_id, session_date)
    // =========================================================================

    @Test
    void should_throw_data_integrity_violation_when_saving_two_sessions_for_same_routine_and_date() {
        RoutineEntity routine = persistRoutine("Push Day");
        LocalDate sameDate = LocalDate.of(2026, 11, 5);

        // Primera sesión: se guarda correctamente
        sessionRepository.saveAndFlush(buildSession(sameDate, routine));

        // Segunda sesión con la misma rutina y el mismo día: debe violar el UNIQUE.
        // saveAndFlush() fuerza el flush inmediato para que MySQL evalúe el constraint
        // dentro de esta misma transacción, antes del rollback automático del test.
        assertThatThrownBy(() ->
                sessionRepository.saveAndFlush(buildSession(sameDate, routine))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void should_allow_two_sessions_on_same_date_if_routines_are_different() {
        // El UNIQUE es (routine_id, session_date): distinta rutina + mismo día no debe fallar
        RoutineEntity push = persistRoutine("Push Day");
        RoutineEntity pull = persistRoutine("Pull Day");
        LocalDate sameDate = LocalDate.of(2026, 11, 5);

        sessionRepository.saveAndFlush(buildSession(sameDate, push));

        // Esto no debe lanzar ninguna excepción
        WorkoutSessionEntity saved = sessionRepository.saveAndFlush(buildSession(sameDate, pull));
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void should_allow_multiple_sessions_with_null_routine_on_same_date() {
        // NULL no cumple el UNIQUE en MySQL: varios NULL no chocan entre sí
        LocalDate sameDate = LocalDate.of(2026, 11, 5);

        sessionRepository.saveAndFlush(buildSession(sameDate, null));
        WorkoutSessionEntity saved = sessionRepository.saveAndFlush(buildSession(sameDate, null));

        assertThat(saved.getId()).isNotNull();
    }

    // =========================================================================
    // ON DELETE SET NULL: borrar una rutina no borra el historial
    // =========================================================================

    @Test
    void should_set_routine_to_null_on_session_when_routine_is_deleted() {
        RoutineEntity routine = persistRoutine("Push Day");
        WorkoutSessionEntity session = sessionRepository.saveAndFlush(
                buildSession(LocalDate.of(2026, 11, 1), routine)
        );

        // Limpiamos la caché L1 ANTES de borrar la rutina.
        // Si no lo hacemos, la WorkoutSessionEntity sigue "managed" con referencia a routine.
        // Al llamar delete(routine), Hibernate la marca "removed". Cuando flush() procesa
        // todas las entidades pendientes, ve la sesión apuntando a una entidad "removed"
        // y lanza TransientPropertyValueException.
        // Con clear() la sesión queda "detached" y Hibernate no la incluye en el flush.
        entityManager.clear();

        // Borramos la rutina. MySQL ejecuta ON DELETE SET NULL en workout_sessions.routine_id.
        // delete() sobre una entidad detached la re-mergea automáticamente antes de borrarla.
        routineRepository.delete(routine);
        routineRepository.flush();

        WorkoutSessionEntity refetched = sessionRepository.findById(session.getId()).orElseThrow();
        assertThat(refetched.getRoutine()).isNull();
        // El snapshot del nombre se conserva aunque la rutina ya no exista
        assertThat(refetched.getRoutineName()).isEqualTo("Push Day");
    }

    // =========================================================================
    // Cascade: persistir una sesión con ejercicios y series en cascada
    // =========================================================================

    @Test
    void should_persist_logged_exercises_and_sets_via_cascade_when_saving_session() {
        ExerciseTemplateEntity template = persistTemplate();
        RoutineEntity routine = persistRoutine("Push Day");

        // Construimos el árbol: sesión → ejercicio → serie
        // La cascada ALL en WorkoutSessionEntity.exercises y LoggedExerciseEntity.sets
        // propagará el persist hacia los hijos sin que tengamos que guardarlos por separado.
        LoggedSetEntity set = LoggedSetEntity.builder()
                .numberSet(1)
                .weightKg(new BigDecimal("80.00"))
                .reps(8)
                .rpe(8)
                .build();

        LoggedExerciseEntity exercise = LoggedExerciseEntity.builder()
                .exerciseTemplate(template)
                .orderInSession(1)
                .build();
        exercise.getSets().add(set);
        set.setLoggedExercise(exercise); // lado propietario de la relación

        WorkoutSessionEntity session = buildSession(LocalDate.of(2026, 11, 1), routine);
        session.getExercises().add(exercise);
        exercise.setWorkoutSession(session); // lado propietario de la relación

        WorkoutSessionEntity saved = sessionRepository.saveAndFlush(session);

        // Limpiamos la caché para que el find recargue todo desde la BD
        entityManager.clear();

        WorkoutSessionEntity loaded = sessionRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getExercises()).hasSize(1);
        assertThat(loaded.getExercises().get(0).getSets()).hasSize(1);
        assertThat(loaded.getExercises().get(0).getSets().get(0).getWeightKg())
                .isEqualByComparingTo(new BigDecimal("80.00"));
    }
}
