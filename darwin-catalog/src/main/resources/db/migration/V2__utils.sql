DELIMITER $$

drop procedure if exists CreateIndexIfNotExists$$
CREATE PROCEDURE CreateIndexIfNotExists(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64)
)
BEGIN
    DECLARE index_exists INT DEFAULT 0;
    DECLARE index_name_1 VARCHAR(128);
    DECLARE sql_statement TEXT;

    SET index_name_1 = CONCAT('idx_', p_table_name, '_', p_column_name);

    -- Check if index exists
    SELECT COUNT(1) INTO index_exists
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND index_name = index_name_1;

    -- Create index if it doesn't exist
    IF index_exists = 0 THEN
        SET sql_statement = CONCAT('CREATE INDEX ', index_name_1, ' ON ', p_table_name, '(', p_column_name, ')');
        SET @sql = sql_statement;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

drop procedure if exists AddColumnIfNotExists$$

CREATE PROCEDURE AddColumnIfNotExists(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_definition TEXT
)
BEGIN
    DECLARE column_exists INT DEFAULT 0;
    DECLARE sql_statement TEXT;

    -- Check if column exists
    SELECT COUNT(1) INTO column_exists
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND column_name = p_column_name;

    -- Add column if it doesn't exist
    IF column_exists = 0 THEN
        SET sql_statement = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN ', p_column_name, ' ', p_column_definition);
        SET @sql = sql_statement;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

drop procedure if exists AddEnumValueIfNotExists$$
CREATE PROCEDURE AddEnumValueIfNotExists(
    IN table_name VARCHAR(64),
    IN column_name VARCHAR(64),
    IN new_value VARCHAR(255)
)
BEGIN
    DECLARE enum_type TEXT;
    DECLARE new_enum_type TEXT;
    DECLARE alter_sql TEXT;

    -- Get current enum definition
    SELECT COLUMN_TYPE INTO enum_type
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = table_name
      AND COLUMN_NAME = column_name
      AND DATA_TYPE = 'enum'
    LIMIT 1;

    -- Only proceed if the value does not exist
    IF enum_type IS NOT NULL AND LOCATE(CONCAT('''', new_value, ''''), enum_type) = 0 THEN
        -- Insert the new value before the last parenthesis
        SET new_enum_type = INSERT(enum_type, LENGTH(enum_type), 0, CONCAT(',''', new_value, ''''));
        SET alter_sql = CONCAT('ALTER TABLE `', table_name, '` MODIFY COLUMN `', column_name, '` ', new_enum_type, ' NOT NULL;');
        SET @sql = alter_sql;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

drop procedure if exists RemoveEnumValueIfExists$$
CREATE PROCEDURE RemoveEnumValueIfExists(
    IN table_name VARCHAR(64),
    IN column_name VARCHAR(64),
    IN remove_value VARCHAR(255)
)
BEGIN
    DECLARE enum_type TEXT;
    DECLARE new_enum_type TEXT;
    DECLARE alter_sql TEXT;

    -- Get current enum definition
    SELECT COLUMN_TYPE INTO enum_type
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = table_name
      AND COLUMN_NAME = column_name
      AND DATA_TYPE = 'enum'
    LIMIT 1;

    -- Only proceed if the value exists
    IF enum_type IS NOT NULL AND LOCATE(CONCAT('''', remove_value, ''''), enum_type) > 0 THEN
        -- Remove the value and any preceding comma
        SET new_enum_type = TRIM(BOTH ',' FROM REPLACE(
                REPLACE(enum_type, CONCAT(',', '''', remove_value, ''''), ''),
                CONCAT('''', remove_value, '''', ','), ''
                                               ));
        -- Remove the value if it's the only one left
        SET new_enum_type = REPLACE(new_enum_type, CONCAT('''', remove_value, ''''), '');
        -- Clean up double commas
        SET new_enum_type = REPLACE(new_enum_type, ',,', ',');
        -- Remove trailing/leading commas
        SET new_enum_type = REPLACE(new_enum_type, '(,', '(');
        SET new_enum_type = REPLACE(new_enum_type, ',)', ')');
        -- If all values are removed, do nothing
        IF LENGTH(REPLACE(REPLACE(new_enum_type, 'enum(', ''), ')', '')) > 0 THEN
            SET alter_sql = CONCAT('ALTER TABLE `', table_name, '` MODIFY COLUMN `', column_name, '` ', new_enum_type, ' NOT NULL;');
            SET @sql = alter_sql;
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END IF;
    END IF;
END$$

drop procedure if exists DropColumnIfExists$$
CREATE PROCEDURE DropColumnIfExists(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64)
)
BEGIN
    DECLARE v_sql TEXT;
    DECLARE v_col_exists INT DEFAULT 0;

    -- Check if the column exists in the current database
    SELECT COUNT(1) INTO v_col_exists
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND column_name = p_column_name;

    -- If it exists, prepare and execute the drop statement
    IF v_col_exists > 0 THEN
        SET v_sql = CONCAT('ALTER TABLE ', p_table_name, ' DROP COLUMN ', p_column_name);
        SET @sql = v_sql;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

drop procedure if exists DropConstraintIfExists$$
CREATE PROCEDURE DropConstraintIfExists(
    IN p_table_name VARCHAR(64),
    IN p_constraint_name VARCHAR(64)
)
BEGIN
    DECLARE v_sql TEXT;
    DECLARE v_constraint_exists INT DEFAULT 0;
    DECLARE v_constraint_type VARCHAR(64);

    -- Check if the constraint exists and get its type
    SELECT COUNT(1), MAX(constraint_type) INTO v_constraint_exists, v_constraint_type
    FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = p_table_name
      AND constraint_name = p_constraint_name;

    -- If it exists, prepare and execute the appropriate drop statement based on constraint type
    IF v_constraint_exists > 0 THEN
        CASE v_constraint_type
            WHEN 'FOREIGN KEY' THEN
                SET v_sql = CONCAT('ALTER TABLE ', p_table_name, ' DROP FOREIGN KEY ', p_constraint_name);
            WHEN 'UNIQUE' THEN
                SET v_sql = CONCAT('ALTER TABLE ', p_table_name, ' DROP INDEX ', p_constraint_name);
            WHEN 'CHECK' THEN
                SET v_sql = CONCAT('ALTER TABLE ', p_table_name, ' DROP CHECK ', p_constraint_name);
            WHEN 'PRIMARY KEY' THEN
                SET v_sql = CONCAT('ALTER TABLE ', p_table_name, ' DROP PRIMARY KEY');
            ELSE
                -- For other constraint types, try generic drop
                SET v_sql = CONCAT('ALTER TABLE ', p_table_name, ' DROP CONSTRAINT ', p_constraint_name);
            END CASE;

        SET @sql = v_sql;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;
