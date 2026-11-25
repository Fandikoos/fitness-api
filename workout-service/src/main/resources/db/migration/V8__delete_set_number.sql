-- Elimina set_number solo si existe. Se usa INFORMATION_SCHEMA porque MySQL < 8.0.3
-- no soporta DROP COLUMN IF EXISTS. La columna puede no existir en una BD limpia
-- (Testcontainers) porque V4 ya la renombró a number_set.
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'exercise_sets'
      AND COLUMN_NAME  = 'set_number'
);

SET @sql = IF(@col_exists > 0, 'ALTER TABLE exercise_sets DROP COLUMN set_number', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;