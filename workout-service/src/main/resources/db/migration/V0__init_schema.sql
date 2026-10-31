-- =============================================================================
-- V0__init_schema.sql  –  Esquema inicial del workout-service
--
-- Flyway ejecuta este script UNA sola vez (al arrancar la app por primera vez
-- contra una BD vacía) y registra la ejecución en la tabla flyway_schema_history
-- para no volver a aplicarlo. Cada migración posterior tendrá su propio V2, V3...
CREATE TABLE programs (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    -- BIT(1) es como Hibernate mapea un boolean en MySQL
    active      BIT(1)       NOT NULL DEFAULT 0,
    -- DATETIME(6): microsegundos, que es lo que usa LocalDateTime de Java con Hibernate
    created_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id)
);

-- -----------------------------------------------------------------------------
-- exercise_templates
-- Catálogo global de ejercicios disponibles. Independiente de cualquier rutina.
-- -----------------------------------------------------------------------------
CREATE TABLE exercise_templates (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    name         VARCHAR(255) NOT NULL,
    -- VARCHAR(50) es suficiente para los nombres del enum MuscleGroup
    muscle_group VARCHAR(50),
    -- TEXT en vez de VARCHAR(255) porque las URLs pueden ser largas
    image_url    TEXT,
    video_url    TEXT,
    PRIMARY KEY (id)
);

-- -----------------------------------------------------------------------------
-- routines
-- Un día de entrenamiento. Pertenece opcionalmente a un programa.
-- -----------------------------------------------------------------------------
CREATE TABLE routines (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    -- VARCHAR(20): suficiente para MONDAY, TUESDAY... (enum DayOfWeek guardado como STRING)
    day_of_week VARCHAR(20),
    description VARCHAR(255),
    -- Nullable: una rutina puede existir sin estar asignada a un programa todavía
    program_id  BIGINT,
    created_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_routines_program
        FOREIGN KEY (program_id) REFERENCES programs (id)
);

-- -----------------------------------------------------------------------------
-- routine_exercises
-- Asignación de un ejercicio del catálogo a una rutina concreta.
-- Es la tabla "puente" que une ExerciseTemplate con Routine.
-- -----------------------------------------------------------------------------
CREATE TABLE routine_exercises (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    exercise_template_id BIGINT       NOT NULL,
    routine_id           BIGINT       NOT NULL,
    order_in_routine     INT          NOT NULL,
    notes                VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_routine_exercises_template
        FOREIGN KEY (exercise_template_id) REFERENCES exercise_templates (id),
    CONSTRAINT fk_routine_exercises_routine
        FOREIGN KEY (routine_id) REFERENCES routines (id)
);

-- -----------------------------------------------------------------------------
-- exercise_sets
-- Serie individual de un ejercicio en una rutina (peso, reps, RPE).
-- -----------------------------------------------------------------------------
CREATE TABLE exercise_sets (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    set_number          INT           NOT NULL,
    -- DECIMAL(5,2): hasta 999.99 kg, evita errores de redondeo de float/double
    weight_kg           DECIMAL(5, 2) NOT NULL,
    reps                INT           NOT NULL,
    -- Nullable: el RPE es opcional, no siempre se registra
    rpe                 INT,
    routine_exercise_id BIGINT        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_exercise_sets_routine_exercise
        FOREIGN KEY (routine_exercise_id) REFERENCES routine_exercises (id)
);