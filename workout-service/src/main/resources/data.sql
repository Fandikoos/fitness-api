-- ============================================================
-- Datos de prueba para desarrollo/frontend
-- Spring Boot ejecuta este archivo automáticamente al arrancar
-- si spring.sql.init.mode=always está configurado.
--
-- INSERT IGNORE evita errores si los datos ya existen (idempotente).
-- Para exercise_sets usamos NOT EXISTS porque no tienen ID explícito.
-- ============================================================

-- Catálogo de ejercicios (exercise_templates)
-- Estos ejercicios existen independientemente de cualquier rutina.
-- El usuario los elige al configurar su rutina.

-- Pecho
INSERT IGNORE INTO exercise_templates (id, name, muscle_group, image_url, video_url) VALUES
(1,  'Press Banca',                      'CHEST',      NULL, NULL),
(2,  'Press Inclinado con Mancuernas',   'CHEST',      NULL, NULL),
(3,  'Aperturas en Polea',               'CHEST',      NULL, NULL),
(4,  'Press Declinado',                  'CHEST',      NULL, NULL);

-- Hombros
INSERT IGNORE INTO exercise_templates (id, name, muscle_group, image_url, video_url) VALUES
(5,  'Press Militar',                    'SHOULDERS',  NULL, NULL),
(6,  'Elevaciones Laterales',            'SHOULDERS',  NULL, NULL),
(7,  'Pájaros (Elevaciones Posteriores)','SHOULDERS',  NULL, NULL);

-- Tríceps
INSERT IGNORE INTO exercise_templates (id, name, muscle_group, image_url, video_url) VALUES
(8,  'Fondos en Paralelas',              'TRICEPS',    NULL, NULL),
(9,  'Extensión de Tríceps en Polea',    'TRICEPS',    NULL, NULL),
(10, 'Press Francés',                    'TRICEPS',    NULL, NULL);

-- Espalda
INSERT IGNORE INTO exercise_templates (id, name, muscle_group, image_url, video_url) VALUES
(11, 'Dominadas',                        'BACK',       NULL, NULL),
(12, 'Remo con Barra',                  'BACK',       NULL, NULL),
(13, 'Jalón al Pecho',                  'BACK',       NULL, NULL),
(14, 'Remo en Polea Baja',              'BACK',       NULL, NULL);

-- Trapecios
INSERT IGNORE INTO exercise_templates (id, name, muscle_group, image_url, video_url) VALUES
(15, 'Face Pull',                        'TRAPS',      NULL, NULL),
(16, 'Encogimientos con Mancuernas',     'TRAPS',      NULL, NULL);

-- Bíceps
INSERT IGNORE INTO exercise_templates (id, name, muscle_group, image_url, video_url) VALUES
(17, 'Curl con Barra',                  'BICEPS',     NULL, NULL),
(18, 'Curl Martillo',                   'BICEPS',     NULL, NULL),
(19, 'Curl Concentrado',                'BICEPS',     NULL, NULL);

-- Antebrazos
INSERT IGNORE INTO exercise_templates (id, name, muscle_group, image_url, video_url) VALUES
(20, 'Curl de Muñeca',                  'FOREARMS',   NULL, NULL);

-- Cuádriceps
INSERT IGNORE INTO exercise_templates (id, name, muscle_group, image_url, video_url) VALUES
(21, 'Sentadilla con Barra',            'QUADRICEPS', NULL, NULL),
(22, 'Prensa de Piernas',               'QUADRICEPS', NULL, NULL),
(23, 'Extensión de Cuádriceps',         'QUADRICEPS', NULL, NULL);

-- Isquiotibiales
INSERT IGNORE INTO exercise_templates (id, name, muscle_group, image_url, video_url) VALUES
(24, 'Peso Muerto Rumano',              'HAMSTRINGS', NULL, NULL),
(25, 'Curl Femoral Tumbado',            'HAMSTRINGS', NULL, NULL);

-- Glúteos
INSERT IGNORE INTO exercise_templates (id, name, muscle_group, image_url, video_url) VALUES
(26, 'Hip Thrust',                      'GLUTES',     NULL, NULL);

-- Gemelos
INSERT IGNORE INTO exercise_templates (id, name, muscle_group, image_url, video_url) VALUES
(27, 'Elevación de Gemelos de Pie',     'CALVES',     NULL, NULL),
(28, 'Elevación de Gemelos Sentado',    'CALVES',     NULL, NULL);

-- Abdominales
INSERT IGNORE INTO exercise_templates (id, name, muscle_group, image_url, video_url) VALUES
(29, 'Crunch Abdominal',               'ABS',        NULL, NULL),
(30, 'Plancha',                         'ABS',        NULL, NULL);

-- ============================================================
-- Rutinas de ejemplo (3 días de entrenamiento)
-- ============================================================
INSERT IGNORE INTO routines (id, name, day_of_week, description, created_at) VALUES
(1, 'Push Day - Pecho, Hombros y Tríceps', 'MONDAY',    'Día de empuje: enfoque en pecho, hombros y tríceps', NOW()),
(2, 'Pull Day - Espalda y Bíceps',         'WEDNESDAY', 'Día de tirón: espalda, bíceps y antebrazos',         NOW()),
(3, 'Leg Day - Piernas',                   'FRIDAY',    'Día de piernas: cuádriceps, isquios, glúteos y gemelos', NOW());

-- ============================================================
-- Ejercicios vinculados a rutinas (routine_exercises)
-- Cada fila conecta un template del catálogo con una rutina,
-- añadiendo orden y notas específicas para esa rutina.
-- ============================================================

-- Push Day (routine_id = 1)
INSERT IGNORE INTO routine_exercises (id, exercise_template_id, routine_id, order_in_routine, notes) VALUES
(1,  1,  1, 1, 'Agarre medio, bajar hasta el pecho'),
(2,  2,  1, 2, 'Banco a 30-45 grados'),
(3,  3,  1, 3, 'Cruzar ligeramente las manos al final'),
(4,  5,  1, 4, 'De pie o sentado, sin impulso de piernas'),
(5,  6,  1, 5, 'Peso ligero, controlar la bajada'),
(6,  8,  1, 6, 'Inclinarse hacia adelante para más pecho'),
(7,  9,  1, 7, 'Codos pegados al cuerpo');

-- Pull Day (routine_id = 2)
INSERT IGNORE INTO routine_exercises (id, exercise_template_id, routine_id, order_in_routine, notes) VALUES
(8,  11, 2, 1, 'Agarre prono, ancho de hombros'),
(9,  12, 2, 2, 'Espalda recta, tirar hacia el ombligo'),
(10, 13, 2, 3, 'Agarre amplio, apretar escápulas'),
(11, 14, 2, 4, 'Agarre cerrado, squeeze al final'),
(12, 15, 2, 5, 'Polea alta, tirar hacia la frente'),
(13, 17, 2, 6, 'Sin balanceo, subida controlada'),
(14, 18, 2, 7, 'Trabaja braquial y braquiorradial'),
(15, 20, 2, 8, 'Antebrazos apoyados en el banco');

-- Leg Day (routine_id = 3)
INSERT IGNORE INTO routine_exercises (id, exercise_template_id, routine_id, order_in_routine, notes) VALUES
(16, 21, 3, 1, 'Profundidad paralela o más'),
(17, 22, 3, 2, 'Pies a la anchura de hombros'),
(18, 23, 3, 3, 'Movimiento controlado, sin impulso'),
(19, 24, 3, 4, 'Rodillas ligeramente flexionadas, sentir isquios'),
(20, 25, 3, 5, 'Apretar arriba, bajar lento'),
(21, 26, 3, 6, 'Barra sobre la cadera, squeeze arriba'),
(22, 27, 3, 7, 'Rango completo, pausa abajo'),
(23, 28, 3, 8, 'Trabaja más el sóleo');

-- ============================================================
-- Series de ejemplo para algunos ejercicios en rutina
-- ============================================================

-- Press Banca (routine_exercise_id = 1): 4 series piramidales
INSERT INTO exercise_sets (set_number, weight_kg, reps, rpe, routine_exercise_id)
SELECT s.set_number, s.weight_kg, s.reps, s.rpe, s.routine_exercise_id FROM (
    SELECT 1 AS set_number, 60.00 AS weight_kg, 12 AS reps, 6 AS rpe, 1 AS routine_exercise_id UNION ALL
    SELECT 2, 80.00, 10, 7, 1 UNION ALL
    SELECT 3, 90.00, 8, 8, 1 UNION ALL
    SELECT 4, 95.00, 6, 9, 1
) s WHERE NOT EXISTS (SELECT 1 FROM exercise_sets WHERE routine_exercise_id = 1);

-- Sentadilla (routine_exercise_id = 16): 4 series
INSERT INTO exercise_sets (set_number, weight_kg, reps, rpe, routine_exercise_id)
SELECT s.set_number, s.weight_kg, s.reps, s.rpe, s.routine_exercise_id FROM (
    SELECT 1 AS set_number, 80.00 AS weight_kg, 10 AS reps, 6 AS rpe, 16 AS routine_exercise_id UNION ALL
    SELECT 2, 100.00, 8, 7, 16 UNION ALL
    SELECT 3, 110.00, 6, 8, 16 UNION ALL
    SELECT 4, 120.00, 5, 9, 16
) s WHERE NOT EXISTS (SELECT 1 FROM exercise_sets WHERE routine_exercise_id = 16);

-- Dominadas (routine_exercise_id = 8): 3 series al peso corporal
INSERT INTO exercise_sets (set_number, weight_kg, reps, rpe, routine_exercise_id)
SELECT s.set_number, s.weight_kg, s.reps, s.rpe, s.routine_exercise_id FROM (
    SELECT 1 AS set_number, 0.00 AS weight_kg, 12 AS reps, 7 AS rpe, 8 AS routine_exercise_id UNION ALL
    SELECT 2, 0.00, 10, 8, 8 UNION ALL
    SELECT 3, 0.00, 8, 9, 8
) s WHERE NOT EXISTS (SELECT 1 FROM exercise_sets WHERE routine_exercise_id = 8);

-- Curl con Barra (routine_exercise_id = 13): 3 series
INSERT INTO exercise_sets (set_number, weight_kg, reps, rpe, routine_exercise_id)
SELECT s.set_number, s.weight_kg, s.reps, s.rpe, s.routine_exercise_id FROM (
    SELECT 1 AS set_number, 30.00 AS weight_kg, 12 AS reps, 7 AS rpe, 13 AS routine_exercise_id UNION ALL
    SELECT 2, 35.00, 10, 8, 13 UNION ALL
    SELECT 3, 40.00, 8, 9, 13
) s WHERE NOT EXISTS (SELECT 1 FROM exercise_sets WHERE routine_exercise_id = 13);
