DROP PROCEDURE IF EXISTS ModifyColumnTypeIfNeeded;
CREATE PROCEDURE ModifyColumnTypeIfNeeded(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_new_column_definition TEXT
) BEGIN
DECLARE column_exists INT DEFAULT 0;
DECLARE current_column_type VARCHAR(255);
DECLARE sql_statement TEXT;
-- Check if column exists and get its current type
SELECT COUNT(1),
    COALESCE(MAX(COLUMN_TYPE), '') INTO column_exists,
    current_column_type
FROM information_schema.columns
WHERE table_schema = DATABASE()
    AND table_name = p_table_name
    AND column_name = p_column_name;
-- Modify column if it exists and the type is different
IF column_exists > 0 THEN -- Check if current type is varchar(255) and we want to change it to text
IF current_column_type LIKE 'varchar(255)%'
AND p_new_column_definition LIKE 'TEXT%' THEN
SET sql_statement = CONCAT(
        'ALTER TABLE ',
        p_table_name,
        ' MODIFY COLUMN ',
        p_column_name,
        ' ',
        p_new_column_definition
    );
SET @sql = sql_statement;
PREPARE stmt
FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SELECT CONCAT(
        'Column ',
        p_column_name,
        ' in table ',
        p_table_name,
        ' modified from ',
        current_column_type,
        ' to ',
        p_new_column_definition
    ) as message;
ELSE
SELECT CONCAT(
        'Column ',
        p_column_name,
        ' in table ',
        p_table_name,
        ' already has appropriate type: ',
        current_column_type
    ) as message;
END IF;
ELSE
SELECT CONCAT(
        'Column ',
        p_column_name,
        ' does not exist in table ',
        p_table_name
    ) as message;
END IF;
END;
-- Expand the description column in the asset table from VARCHAR(255) to TEXT
-- This is idempotent - it will only modify if the column exists and is currently VARCHAR(255)
CALL ModifyColumnTypeIfNeeded('asset', 'description', 'TEXT');
-- Clean up the procedure
DROP PROCEDURE IF EXISTS ModifyColumnTypeIfNeeded;
