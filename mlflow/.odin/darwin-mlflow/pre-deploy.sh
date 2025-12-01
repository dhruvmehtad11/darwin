#!/usr/bin/env bash
set -e

echo "APP_DIR: ${APP_DIR}"
echo "ENV: ${ENV}"
echo "SERVICE_NAME: ${SERVICE_NAME}"
echo "VAULT_SERVICE_MYSQL_USERNAME: ${VAULT_SERVICE_MYSQL_USERNAME:-darwin}"
echo "VAULT_SERVICE_MYSQL_PASSWORD: ${VAULT_SERVICE_MYSQL_PASSWORD:-password}"
echo "CONFIG_SERVICE_MYSQL_MASTERHOST: ${CONFIG_SERVICE_MYSQL_MASTERHOST:-darwin-mysql}"
echo "CONFIG_SERVICE_MYSQL_DATABASE: ${CONFIG_SERVICE_MYSQL_DATABASE:-darwin_mlflow}"

# Database initialization for MLflow
# This script creates the required databases if they don't exist
# Note: darwin-mlflow (mlflow-lib) is the primary service that uses the MySQL database
# for MLflow tracking backend. The database initialization is done here.

MYSQL_HOST="${CONFIG_SERVICE_MYSQL_MASTERHOST:-${DARWIN_MYSQL_HOST:-darwin-mysql}}"
MYSQL_USER="${DARWIN_MYSQL_USERNAME:-root}"
MYSQL_PASSWORD="${DARWIN_MYSQL_PASSWORD:-password}"
MYSQL_PORT="${MYSQL_PORT:-3306}"

# MLflow databases
MLFLOW_DB="${CONFIG_SERVICE_MYSQL_DATABASE:-darwin_mlflow}"
MLFLOW_AUTH_DB="${CONFIG_SERVICE_MYSQL_AUTH_DATABASE:-darwin_mlflow_auth}"
``
# Database user for MLflow (from vault or default)
DB_USER="${VAULT_SERVICE_MYSQL_USERNAME:-darwin}"
DB_PASSWORD="${VAULT_SERVICE_MYSQL_PASSWORD:-password}"

echo "🔧 Initializing MLflow databases..."
echo "   MySQL Host: ${MYSQL_HOST}:${MYSQL_PORT}"
echo "   Creating databases: ${MLFLOW_DB}, ${MLFLOW_AUTH_DB}"

# Wait for MySQL to be ready
echo "⏳ Waiting for MySQL to be ready..."
max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
  if mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_USER}" -p"${MYSQL_PASSWORD}" -e "SELECT 1" >/dev/null 2>&1; then
    echo "✅ MySQL is ready"
    break
  fi
  attempt=$((attempt + 1))
  echo "   Attempt ${attempt}/${max_attempts} - MySQL not ready yet, waiting..."
  sleep 2
done

if [ $attempt -eq $max_attempts ]; then
  echo "❌ Failed to connect to MySQL after ${max_attempts} attempts"
  exit 1
fi

# Create databases and grant privileges
echo "📦 Creating databases and setting up privileges..."

mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_USER}" -p"${MYSQL_PASSWORD}" <<EOF
CREATE DATABASE IF NOT EXISTS ${MLFLOW_DB};
CREATE DATABASE IF NOT EXISTS ${MLFLOW_AUTH_DB};
GRANT ALL PRIVILEGES ON ${MLFLOW_DB}.* TO '${DB_USER}'@'%';
GRANT ALL PRIVILEGES ON ${MLFLOW_AUTH_DB}.* TO '${DB_USER}'@'%';
FLUSH PRIVILEGES;
EOF

if [ $? -eq 0 ]; then
  echo "✅ Successfully created databases and granted privileges"
  echo "   - Database: ${MLFLOW_DB}"
  echo "   - Database: ${MLFLOW_AUTH_DB}"
  echo "   - User: ${DB_USER}"
  echo "✅ MLflow database initialization completed"
else
  echo "❌ Failed to create databases"
  exit 1
fi
