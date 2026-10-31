-- =============================================================================
-- V1__seed_exercise_catalog.sql  –  Catálogo inicial de ejercicios
--
-- Datos de referencia (reference data): la app los necesita para funcionar,
-- no son datos de prueba. Sin este catálogo el selector de ejercicios del
-- frontend se queda vacío.
--
-- Imágenes: wger.de (CC-BY-SA). Formato consistente: ilustraciones en blanco
-- y negro con diagrama muscular. Las que no tienen imagen en wger quedan NULL
-- para rellenar en una migración posterior.
--
-- Flyway aplica este script una sola vez y lo registra en flyway_schema_history.
-- =============================================================================

-- ─── CHEST (Pecho) ──────────────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Press de banca con barra',        'CHEST', 'https://wger.de/media/exercise-images/192/Bench-press-1.png', NULL),
('Press de banca inclinado',        'CHEST', 'https://wger.de/media/exercise-images/41/Incline-bench-press-1.png', NULL),
('Press de banca declinado',        'CHEST', 'https://wger.de/media/exercise-images/100/Decline-bench-press-1.png', NULL),
('Press con mancuernas',            'CHEST', 'https://wger.de/media/exercise-images/97/Dumbbell-bench-press-1.png', NULL),
('Press inclinado con mancuernas',  'CHEST', 'https://wger.de/media/exercise-images/16/Incline-press-1.png', NULL),
('Aperturas con mancuernas',        'CHEST', 'https://wger.de/media/exercise-images/98/Butterfly-machine-2.png', NULL),
('Cruces en polea (cable fly)',     'CHEST', 'https://wger.de/media/exercise-images/71/Cable-crossover-2.png', NULL),
('Fondos en paralelas (pecho)',     'CHEST', 'https://wger.de/media/exercise-images/194/34600351-8b0b-4cb0-8daa-583537be15b0.png', NULL),
('Press en máquina',                'CHEST', NULL, NULL);

-- ─── BACK (Espalda) ─────────────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Dominadas (pull-ups)',             'BACK', 'https://wger.de/media/exercise-images/152/6c1a7459-266d-491a-bd50-7cbaea2bc771.png', NULL),
('Jalón al pecho (lat pulldown)',    'BACK', 'https://wger.de/media/exercise-images/158/0d51a0f2-622f-434b-beb8-1a003c54712a.png', NULL),
('Remo con barra',                   'BACK', 'https://wger.de/media/exercise-images/110/Reverse-grip-bent-over-rows-1.png', NULL),
('Remo con mancuerna',               'BACK', 'https://wger.de/media/exercise-images/81/a751a438-ae2d-4751-8d61-cef0e9292174.png', NULL),
('Remo en polea baja (seated row)',  'BACK', 'https://wger.de/media/exercise-images/512/b938437e-ff00-4679-9036-acb41bb28bbd.png', NULL),
('Peso muerto convencional',         'BACK', 'https://wger.de/media/exercise-images/161/Dead-lifts-2.png', NULL),
('Pullover con mancuerna',           'BACK', NULL, NULL),
('Remo en máquina T-bar',           'BACK', 'https://wger.de/media/exercise-images/106/T-bar-row-1.png', NULL);

-- ─── SHOULDERS (Hombros) ────────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Press militar con barra',          'SHOULDERS', 'https://wger.de/media/exercise-images/119/seated-barbell-shoulder-press-large-1.png', NULL),
('Press con mancuernas (hombro)',    'SHOULDERS', 'https://wger.de/media/exercise-images/123/dumbbell-shoulder-press-large-1.png', NULL),
('Elevaciones laterales',            'SHOULDERS', 'https://wger.de/media/exercise-images/148/lateral-dumbbell-raises-large-2.png', NULL),
('Elevaciones frontales',            'SHOULDERS', NULL, NULL),
('Pájaros (reverse fly)',            'SHOULDERS', NULL, NULL),
('Face pulls',                       'SHOULDERS', NULL, NULL),
('Press Arnold',                     'SHOULDERS', NULL, NULL),
('Elevaciones laterales en polea',   'SHOULDERS', NULL, NULL);

-- ─── BICEPS ─────────────────────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Curl con barra recta',             'BICEPS', 'https://wger.de/media/exercise-images/81/Biceps-curl-1.png', NULL),
('Curl con barra Z',                 'BICEPS', 'https://wger.de/media/exercise-images/129/Standing-biceps-curl-1.png', NULL),
('Curl con mancuernas alterno',      'BICEPS', 'https://wger.de/media/exercise-images/74/Bicep-curls-1.png', NULL),
('Curl martillo',                    'BICEPS', 'https://wger.de/media/exercise-images/86/Bicep-hammer-curl-1.png', NULL),
('Curl concentrado',                 'BICEPS', NULL, NULL),
('Curl en polea baja',               'BICEPS', NULL, NULL),
('Curl en banco Scott (predicador)', 'BICEPS', 'https://wger.de/media/exercise-images/193/Preacher-curl-3-1.png', NULL);

-- ─── TRICEPS ────────────────────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Extensión de tríceps en polea (pushdown)', 'TRICEPS', NULL, NULL),
('Extensión de tríceps con cuerda',          'TRICEPS', NULL, NULL),
('Press francés (skull crusher)',            'TRICEPS', 'https://wger.de/media/exercise-images/84/Lying-close-grip-triceps-press-to-chin-1.png', NULL),
('Extensión de tríceps sobre cabeza',        'TRICEPS', 'https://wger.de/media/exercise-images/50/695ced5c-9961-4076-add2-cb250d01089e.png', NULL),
('Fondos en paralelas (tríceps)',            'TRICEPS', 'https://wger.de/media/exercise-images/194/34600351-8b0b-4cb0-8daa-583537be15b0.png', NULL),
('Patada de tríceps (kickback)',             'TRICEPS', NULL, NULL),
('Press cerrado con barra',                  'TRICEPS', 'https://wger.de/media/exercise-images/61/Close-grip-bench-press-1.png', NULL);

-- ─── QUADRICEPS (Cuádriceps) ────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Sentadilla con barra (back squat)',  'QUADRICEPS', NULL, NULL),
('Sentadilla frontal (front squat)',   'QUADRICEPS', 'https://wger.de/media/exercise-images/191/Front-squat-1-857x1024.png', NULL),
('Prensa de piernas',                  'QUADRICEPS', NULL, NULL),
('Extensión de cuádriceps (máquina)',  'QUADRICEPS', NULL, NULL),
('Sentadilla búlgara',                 'QUADRICEPS', NULL, NULL),
('Zancadas (lunges)',                   'QUADRICEPS', 'https://wger.de/media/exercise-images/113/Walking-lunges-1.png', NULL),
('Sentadilla hack',                     'QUADRICEPS', 'https://wger.de/media/exercise-images/130/Narrow-stance-hack-squats-1-1024x721.png', NULL);

-- ─── HAMSTRINGS (Isquiotibiales) ────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Curl femoral tumbado (máquina)',     'HAMSTRINGS', 'https://wger.de/media/exercise-images/154/lying-leg-curl-machine-large-1.png', NULL),
('Curl femoral sentado (máquina)',     'HAMSTRINGS', 'https://wger.de/media/exercise-images/117/seated-leg-curl-large-1.png', NULL),
('Peso muerto rumano con barra',       'HAMSTRINGS', 'https://wger.de/media/exercise-images/184/1709c405-620a-4d07-9658-fade2b66a2df.jpeg', NULL),
('Peso muerto rumano con mancuernas',  'HAMSTRINGS', NULL, NULL),
('Buenos días (good mornings)',        'HAMSTRINGS', NULL, NULL),
('Hip thrust con barra',              'HAMSTRINGS', NULL, NULL);

-- ─── GLUTES (Glúteos) ───────────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Hip thrust con barra (glúteo)',      'GLUTES', NULL, NULL),
('Patada de glúteo en polea',          'GLUTES', 'https://wger.de/media/exercise-images/1613/a851fe9d-771f-44da-82f0-799e02ae3fd1.jpg', NULL),
('Sentadilla sumo',                    'GLUTES', 'https://wger.de/media/exercise-images/1612/3dc33f57-2786-4305-8b91-e011d7055923.jpg', NULL),
('Puente de glúteo (glute bridge)',    'GLUTES', 'https://wger.de/media/exercise-images/265/7528acb4-b2cc-4b75-b6ae-d514cbd4f78b.png', NULL),
('Abducción de cadera (máquina)',      'GLUTES', NULL, NULL),
('Step-ups con mancuernas',            'GLUTES', NULL, NULL);

-- ─── CALVES (Gemelos) ───────────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Elevación de gemelos de pie (máquina)',    'CALVES', NULL, NULL),
('Elevación de gemelos sentado (máquina)',   'CALVES', NULL, NULL),
('Elevación de gemelos en prensa',           'CALVES', NULL, NULL),
('Elevación de gemelos con mancuerna',       'CALVES', NULL, NULL);

-- ─── ABS (Abdominales) ──────────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Crunch abdominal',                    'ABS', 'https://wger.de/media/exercise-images/91/Crunches-1.png', NULL),
('Crunch en polea (cable crunch)',      'ABS', NULL, NULL),
('Elevación de piernas colgado',        'ABS', 'https://wger.de/media/exercise-images/125/Leg-raises-2.png', NULL),
('Plancha (plank)',                      'ABS', NULL, NULL),
('Rueda abdominal (ab wheel)',          'ABS', NULL, NULL),
('Mountain climbers',                    'ABS', NULL, NULL),
('Plancha lateral (side plank)',        'ABS', NULL, NULL);

-- ─── FOREARMS (Antebrazos) ──────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Curl de muñeca con barra',            'FOREARMS', NULL, NULL),
('Curl de muñeca invertido',            'FOREARMS', NULL, NULL),
('Curl invertido con barra',            'FOREARMS', NULL, NULL),
('Farmer walk (paseo del granjero)',    'FOREARMS', NULL, NULL);

-- ─── TRAPS (Trapecios) ──────────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Encogimientos con barra (shrugs)',      'TRAPS', 'https://wger.de/media/exercise-images/150/Barbell-shrugs-1.png', NULL),
('Encogimientos con mancuernas',          'TRAPS', 'https://wger.de/media/exercise-images/151/Dumbbell-shrugs-2.png', NULL),
('Remo al mentón (upright row)',          'TRAPS', NULL, NULL),
('Face pulls (trapecios)',                'TRAPS', NULL, NULL),
('Encogimientos en máquina Smith',        'TRAPS', NULL, NULL);
