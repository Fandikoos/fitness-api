CREATE TABLE workout_sessions (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  session_date  DATE         NOT NULL,
  routine_id    BIGINT,                      -- nullable: el historial sobrevive a la rutina
  routine_name  VARCHAR(255) NOT NULL,       -- copia del nombre en el momento de entrenar
  notes         VARCHAR(500),
  created_at    DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_sessions_routine FOREIGN KEY (routine_id)
      REFERENCES routines (id) ON DELETE SET NULL,
  CONSTRAINT uq_session_routine_day UNIQUE (routine_id, session_date)
);

CREATE INDEX idx_sessions_date ON workout_sessions (session_date);

CREATE TABLE logged_exercises (
  id                   BIGINT       NOT NULL AUTO_INCREMENT,
  workout_session_id   BIGINT       NOT NULL,
  exercise_template_id BIGINT       NOT NULL,
  order_in_session     INT          NOT NULL,
  notes                VARCHAR(255),
  PRIMARY KEY (id),
  CONSTRAINT fk_logged_ex_session  FOREIGN KEY (workout_session_id) REFERENCES workout_sessions (id),
  CONSTRAINT fk_logged_ex_template FOREIGN KEY (exercise_template_id) REFERENCES exercise_templates (id)
);

CREATE TABLE logged_sets (
 id                 BIGINT        NOT NULL AUTO_INCREMENT,
 logged_exercise_id BIGINT        NOT NULL,
 number_set         INT           NOT NULL,
 weight_kg          DECIMAL(5, 2) NOT NULL,
 reps               INT           NOT NULL,
 rpe                INT,
 PRIMARY KEY (id),
 CONSTRAINT fk_logged_sets_exercise FOREIGN KEY (logged_exercise_id) REFERENCES logged_exercises (id)
);