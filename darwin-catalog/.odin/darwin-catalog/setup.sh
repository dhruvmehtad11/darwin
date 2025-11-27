#!/usr/bin/env bash
set -e

echo "🔧 Darwin Catalog - Database Setup"
echo "===================================="

# Default values
MYSQL_HOST=${DARWIN_MYSQL_HOST:-localhost}
MYSQL_PORT=${MYSQL_PORT:-3307}
MYSQL_ROOT_USER=${MYSQL_ROOT_USER:-root}
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-root}
MYSQL_DATABASE=${MYSQL_DATABASE:-darwin-catalog}
MYSQL_USER=${MYSQL_USERNAME:-darwin}

echo "📊 Configuration:"
echo "  MySQL Host: ${MYSQL_HOST}:${MYSQL_PORT}"
echo "  Database: ${MYSQL_DATABASE}"
echo "  User: ${MYSQL_USER}"
echo ""

# In container builds we only need to persist default env for pre-deploy; skip MySQL checks
if [ "${DEPLOYMENT_TYPE:-}" = "container" ]; then
  echo "🔧 Container build detected - skipping MySQL checks"
  mkdir -p /app/.odin
  : "${LOAD_TEST_DATA:=true}"
  cat > /app/.odin/env-defaults <<EOF
LOAD_TEST_DATA=${LOAD_TEST_DATA}
EOF
  echo "   LOAD_TEST_DATA default: ${LOAD_TEST_DATA}"
  echo "✅ Container setup.sh completed (env defaults written)"
  exit 0
fi

# Check if mysql client is available
if ! command -v mysql &> /dev/null; then
    echo "❌ Error: mysql client not found. Please install MySQL client."
    echo "   macOS: brew install mysql-client"
    echo "   Ubuntu/Debian: apt-get install mysql-client"
    exit 1
fi

echo "🔍 Checking MySQL connection..."
if ! mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_ROOT_USER}" -p"${MYSQL_ROOT_PASSWORD}" -e "SELECT 1;" &> /dev/null; then
    echo "❌ Error: Could not connect to MySQL server at ${MYSQL_HOST}:${MYSQL_PORT}"
    echo "   Please ensure MySQL is running and credentials are correct."
    exit 1
fi

echo "✅ MySQL connection successful"
echo ""

# Create database if it doesn't exist
echo "📦 Creating database '${MYSQL_DATABASE}' if it doesn't exist..."
mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_ROOT_USER}" -p"${MYSQL_ROOT_PASSWORD}" <<EOF
CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE};
EOF

echo "✅ Database '${MYSQL_DATABASE}' created"
echo ""

# Grant permissions to user
echo "🔑 Granting permissions to user '${MYSQL_USER}'..."
mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_ROOT_USER}" -p"${MYSQL_ROOT_PASSWORD}" <<EOF
GRANT ALL PRIVILEGES ON ${MYSQL_DATABASE}.* TO '${MYSQL_USER}'@'%';
FLUSH PRIVILEGES;
EOF

echo "✅ Permissions granted to user '${MYSQL_USER}'"
echo ""

# Verify database exists and user has access
echo "🔍 Verifying setup..."
DATABASES=$(mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_USER}" -p"${MYSQL_PASSWORD}" -e "SHOW DATABASES LIKE '${MYSQL_DATABASE}';" -sN 2>/dev/null || echo "")

if [[ "$DATABASES" == "${MYSQL_DATABASE}" ]]; then
    echo "✅ User '${MYSQL_USER}' can access database '${MYSQL_DATABASE}'"
else
    echo "⚠️  Warning: User '${MYSQL_USER}' may not have access to database '${MYSQL_DATABASE}'"
fi

echo ""
echo "🎉 Database setup completed successfully!"
echo ""

echo "📝 Next steps:"
echo "   1. Build the application: sh .odin/darwin-catalog/build.sh"
echo "   2. Start the application: sh .odin/darwin-catalog/start.sh"
echo ""
echo "💡 Environment variables used:"
echo "   DARWIN_MYSQL_HOST=${MYSQL_HOST}"
echo "   MYSQL_PORT=${MYSQL_PORT}"
echo "   MYSQL_USERNAME=${MYSQL_USER}"
echo "   MYSQL_PASSWORD=***"
echo "   MYSQL_DATABASE=${MYSQL_DATABASE}"
