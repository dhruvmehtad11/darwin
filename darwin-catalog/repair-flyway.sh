#!/bin/bash

# Script to repair Flyway schema history
# This fixes the failed migration issue

echo "Repairing Flyway schema history..."

# Connect to MySQL and repair the flyway_schema_history table
docker exec -i darwin-catalog-mysql mysql -uroot -prootpassword darwin_catalog <<EOF
-- Delete the failed migration record
DELETE FROM flyway_schema_history WHERE success = 0;

-- If the migration record doesn't exist or we need to reset, we can also do:
-- DELETE FROM flyway_schema_history WHERE version = '1';

-- Or completely reset Flyway history (uncomment if needed):
-- DROP TABLE IF EXISTS flyway_schema_history;
EOF

echo "Flyway schema history repaired. You can now restart the application."

