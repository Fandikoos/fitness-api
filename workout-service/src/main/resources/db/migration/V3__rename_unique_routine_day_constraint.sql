-- Renombra el índice auto-generado 'program_id' a un nombre descriptivo.
-- RENAME INDEX (MySQL 5.7+) solo cambia el nombre; no borra ni recrea el índice,
-- por lo que la FK que lo usa sigue intacta.
ALTER TABLE routines RENAME INDEX program_id TO uq_routine_day_per_program;