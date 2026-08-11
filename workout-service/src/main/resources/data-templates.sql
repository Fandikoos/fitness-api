-- =============================================================================
-- Script de carga inicial: catálogo de ejercicios (exercise_templates)
--
-- Imágenes: todas de musclewiki.com (estilo ilustración anatómica consistente).
-- videoUrl: NULL por ahora, se añadirán más adelante.
--
-- Uso: ejecutar contra la BD de workout-service.
--       Si ya existen registros, el INSERT ignora duplicados gracias a
--       la cláusula INSERT IGNORE (requiere un UNIQUE en name, o bien
--       borrar la tabla antes de ejecutar).
-- =============================================================================

-- ─── CHEST (Pecho) ──────────────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Press de banca con barra',        'CHEST', 'https://musclewiki.com/media/uploads/male-barbell-bench-press-front.png', NULL),
('Press de banca inclinado',        'CHEST', 'https://musclewiki.com/media/uploads/male-barbell-incline-bench-press-front.png', NULL),
('Press de banca declinado',        'CHEST', 'https://musclewiki.com/media/uploads/male-barbell-decline-bench-press-front.png', NULL),
('Press con mancuernas',            'CHEST', 'https://musclewiki.com/media/uploads/male-dumbbell-bench-press-front.png', NULL),
('Press inclinado con mancuernas',  'CHEST', 'https://musclewiki.com/media/uploads/male-dumbbell-incline-bench-press-front.png', NULL),
('Aperturas con mancuernas',        'CHEST', 'https://musclewiki.com/media/uploads/male-dumbbell-fly-front.png', NULL),
('Cruces en polea (cable fly)',     'CHEST', 'https://musclewiki.com/media/uploads/male-cable-fly-front.png', NULL),
('Fondos en paralelas (pecho)',     'CHEST', 'https://musclewiki.com/media/uploads/male-bodyweight-dips-front.png', NULL),
('Press en máquina',                'CHEST', 'https://musclewiki.com/media/uploads/male-machine-chest-press-front.png', NULL);

-- ─── BACK (Espalda) ─────────────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Dominadas (pull-ups)',             'BACK', 'https://musclewiki.com/media/uploads/male-bodyweight-pull-up-front.png', NULL),
('Jalón al pecho (lat pulldown)',    'BACK', 'https://musclewiki.com/media/uploads/male-cable-lat-pulldown-front.png', NULL),
('Remo con barra',                   'BACK', 'https://musclewiki.com/media/uploads/male-barbell-bent-over-row-front.png', NULL),
('Remo con mancuerna',               'BACK', 'https://musclewiki.com/media/uploads/male-dumbbell-row-front.png', NULL),
('Remo en polea baja (seated row)',  'BACK', 'https://musclewiki.com/media/uploads/male-cable-seated-row-front.png', NULL),
('Peso muerto convencional',         'BACK', 'https://musclewiki.com/media/uploads/male-barbell-deadlift-front.png', NULL),
('Pullover con mancuerna',           'BACK', 'https://musclewiki.com/media/uploads/male-dumbbell-pullover-front.png', NULL),
('Remo en máquina T-bar',           'BACK', 'https://musclewiki.com/media/uploads/male-barbell-t-bar-row-front.png', NULL);

-- ─── SHOULDERS (Hombros) ────────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Press militar con barra',          'SHOULDERS', 'https://musclewiki.com/media/uploads/male-barbell-overhead-press-front.png', NULL),
('Press con mancuernas (hombro)',    'SHOULDERS', 'https://musclewiki.com/media/uploads/male-dumbbell-shoulder-press-front.png', NULL),
('Elevaciones laterales',            'SHOULDERS', 'https://musclewiki.com/media/uploads/male-dumbbell-lateral-raise-front.png', NULL),
('Elevaciones frontales',            'SHOULDERS', 'https://musclewiki.com/media/uploads/male-dumbbell-front-raise-front.png', NULL),
('Pájaros (reverse fly)',            'SHOULDERS', 'https://musclewiki.com/media/uploads/male-dumbbell-reverse-fly-front.png', NULL),
('Face pulls',                       'SHOULDERS', 'https://musclewiki.com/media/uploads/male-cable-face-pull-front.png', NULL),
('Press Arnold',                     'SHOULDERS', 'https://musclewiki.com/media/uploads/male-dumbbell-arnold-press-front.png', NULL),
('Elevaciones laterales en polea',   'SHOULDERS', 'https://musclewiki.com/media/uploads/male-cable-lateral-raise-front.png', NULL);

-- ─── BICEPS ─────────────────────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Curl con barra recta',             'BICEPS', 'https://musclewiki.com/media/uploads/male-barbell-curl-front.png', NULL),
('Curl con barra Z',                 'BICEPS', 'https://musclewiki.com/media/uploads/male-barbell-ez-curl-front.png', NULL),
('Curl con mancuernas alterno',      'BICEPS', 'https://musclewiki.com/media/uploads/male-dumbbell-curl-front.png', NULL),
('Curl martillo',                    'BICEPS', 'https://musclewiki.com/media/uploads/male-dumbbell-hammer-curl-front.png', NULL),
('Curl concentrado',                 'BICEPS', 'https://musclewiki.com/media/uploads/male-dumbbell-concentration-curl-front.png', NULL),
('Curl en polea baja',               'BICEPS', 'https://musclewiki.com/media/uploads/male-cable-curl-front.png', NULL),
('Curl en banco Scott (predicador)', 'BICEPS', 'https://musclewiki.com/media/uploads/male-barbell-preacher-curl-front.png', NULL);

-- ─── TRICEPS ────────────────────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Extensión de tríceps en polea (pushdown)', 'TRICEPS', 'https://musclewiki.com/media/uploads/male-cable-pushdown-front.png', NULL),
('Extensión de tríceps con cuerda',          'TRICEPS', 'https://musclewiki.com/media/uploads/male-cable-rope-pushdown-front.png', NULL),
('Press francés (skull crusher)',            'TRICEPS', 'https://musclewiki.com/media/uploads/male-barbell-skull-crusher-front.png', NULL),
('Extensión de tríceps sobre cabeza',        'TRICEPS', 'https://musclewiki.com/media/uploads/male-dumbbell-overhead-tricep-extension-front.png', NULL),
('Fondos en paralelas (tríceps)',            'TRICEPS', 'https://musclewiki.com/media/uploads/male-bodyweight-tricep-dips-front.png', NULL),
('Patada de tríceps (kickback)',             'TRICEPS', 'https://musclewiki.com/media/uploads/male-dumbbell-kickback-front.png', NULL),
('Press cerrado con barra',                  'TRICEPS', 'https://musclewiki.com/media/uploads/male-barbell-close-grip-bench-press-front.png', NULL);

-- ─── QUADRICEPS (Cuádriceps) ────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Sentadilla con barra (back squat)',  'QUADRICEPS', 'https://musclewiki.com/media/uploads/male-barbell-squat-front.png', NULL),
('Sentadilla frontal (front squat)',   'QUADRICEPS', 'https://musclewiki.com/media/uploads/male-barbell-front-squat-front.png', NULL),
('Prensa de piernas',                  'QUADRICEPS', 'https://musclewiki.com/media/uploads/male-machine-leg-press-front.png', NULL),
('Extensión de cuádriceps (máquina)',  'QUADRICEPS', 'https://musclewiki.com/media/uploads/male-machine-leg-extension-front.png', NULL),
('Sentadilla búlgara',                 'QUADRICEPS', 'https://musclewiki.com/media/uploads/male-dumbbell-bulgarian-split-squat-front.png', NULL),
('Zancadas (lunges)',                   'QUADRICEPS', 'https://musclewiki.com/media/uploads/male-dumbbell-lunge-front.png', NULL),
('Sentadilla hack',                     'QUADRICEPS', 'https://musclewiki.com/media/uploads/male-machine-hack-squat-front.png', NULL);

-- ─── HAMSTRINGS (Isquiotibiales) ────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Curl femoral tumbado (máquina)',     'HAMSTRINGS', 'https://musclewiki.com/media/uploads/male-machine-lying-leg-curl-front.png', NULL),
('Curl femoral sentado (máquina)',     'HAMSTRINGS', 'https://musclewiki.com/media/uploads/male-machine-seated-leg-curl-front.png', NULL),
('Peso muerto rumano con barra',       'HAMSTRINGS', 'https://musclewiki.com/media/uploads/male-barbell-romanian-deadlift-front.png', NULL),
('Peso muerto rumano con mancuernas',  'HAMSTRINGS', 'https://musclewiki.com/media/uploads/male-dumbbell-romanian-deadlift-front.png', NULL),
('Buenos días (good mornings)',        'HAMSTRINGS', 'https://musclewiki.com/media/uploads/male-barbell-good-morning-front.png', NULL),
('Hip thrust con barra',              'HAMSTRINGS', 'https://musclewiki.com/media/uploads/male-barbell-hip-thrust-front.png', NULL);

-- ─── GLUTES (Glúteos) ───────────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Hip thrust con barra (glúteo)',      'GLUTES', 'https://musclewiki.com/media/uploads/male-barbell-hip-thrust-front.png', NULL),
('Patada de glúteo en polea',          'GLUTES', 'https://musclewiki.com/media/uploads/male-cable-glute-kickback-front.png', NULL),
('Sentadilla sumo',                    'GLUTES', 'https://musclewiki.com/media/uploads/male-barbell-sumo-squat-front.png', NULL),
('Puente de glúteo (glute bridge)',    'GLUTES', 'https://musclewiki.com/media/uploads/male-bodyweight-glute-bridge-front.png', NULL),
('Abducción de cadera (máquina)',      'GLUTES', 'https://musclewiki.com/media/uploads/male-machine-hip-abduction-front.png', NULL),
('Step-ups con mancuernas',            'GLUTES', 'https://musclewiki.com/media/uploads/male-dumbbell-step-up-front.png', NULL);

-- ─── CALVES (Gemelos) ───────────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Elevación de gemelos de pie (máquina)',    'CALVES', 'https://musclewiki.com/media/uploads/male-machine-standing-calf-raise-front.png', NULL),
('Elevación de gemelos sentado (máquina)',   'CALVES', 'https://musclewiki.com/media/uploads/male-machine-seated-calf-raise-front.png', NULL),
('Elevación de gemelos en prensa',           'CALVES', 'https://musclewiki.com/media/uploads/male-machine-leg-press-calf-raise-front.png', NULL),
('Elevación de gemelos con mancuerna',       'CALVES', 'https://musclewiki.com/media/uploads/male-dumbbell-calf-raise-front.png', NULL);

-- ─── ABS (Abdominales) ──────────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Crunch abdominal',                    'ABS', 'https://musclewiki.com/media/uploads/male-bodyweight-crunch-front.png', NULL),
('Crunch en polea (cable crunch)',      'ABS', 'https://musclewiki.com/media/uploads/male-cable-crunch-front.png', NULL),
('Elevación de piernas colgado',        'ABS', 'https://musclewiki.com/media/uploads/male-bodyweight-hanging-leg-raise-front.png', NULL),
('Plancha (plank)',                      'ABS', 'https://musclewiki.com/media/uploads/male-bodyweight-plank-front.png', NULL),
('Rueda abdominal (ab wheel)',          'ABS', 'https://musclewiki.com/media/uploads/male-bodyweight-ab-wheel-front.png', NULL),
('Mountain climbers',                    'ABS', 'https://musclewiki.com/media/uploads/male-bodyweight-mountain-climber-front.png', NULL),
('Plancha lateral (side plank)',        'ABS', 'https://musclewiki.com/media/uploads/male-bodyweight-side-plank-front.png', NULL);

-- ─── FOREARMS (Antebrazos) ──────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Curl de muñeca con barra',            'FOREARMS', 'https://musclewiki.com/media/uploads/male-barbell-wrist-curl-front.png', NULL),
('Curl de muñeca invertido',            'FOREARMS', 'https://musclewiki.com/media/uploads/male-barbell-reverse-wrist-curl-front.png', NULL),
('Curl invertido con barra',            'FOREARMS', 'https://musclewiki.com/media/uploads/male-barbell-reverse-curl-front.png', NULL),
('Farmer walk (paseo del granjero)',    'FOREARMS', 'https://musclewiki.com/media/uploads/male-dumbbell-farmer-walk-front.png', NULL);

-- ─── TRAPS (Trapecios) ──────────────────────────────────────────────────────
INSERT INTO exercise_templates (name, muscle_group, image_url, video_url) VALUES
('Encogimientos con barra (shrugs)',      'TRAPS', 'https://musclewiki.com/media/uploads/male-barbell-shrug-front.png', NULL),
('Encogimientos con mancuernas',          'TRAPS', 'https://musclewiki.com/media/uploads/male-dumbbell-shrug-front.png', NULL),
('Remo al mentón (upright row)',          'TRAPS', 'https://musclewiki.com/media/uploads/male-barbell-upright-row-front.png', NULL),
('Face pulls (trapecios)',                'TRAPS', 'https://musclewiki.com/media/uploads/male-cable-face-pull-front.png', NULL),
('Encogimientos en máquina Smith',        'TRAPS', 'https://musclewiki.com/media/uploads/male-smith-machine-shrug-front.png', NULL);
