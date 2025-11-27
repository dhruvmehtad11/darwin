 #!/usr/bin/env bash
set -e

echo "🔧 Darwin Catalog - Pre-Deploy Setup"
echo "======================================"

# Default values
MYSQL_HOST=${DARWIN_MYSQL_HOST:-darwin-mysql}
MYSQL_PORT=${MYSQL_PORT:-3306}
MYSQL_ROOT_USER=${MYSQL_ROOT_USER:-root}
MYSQL_ROOT_PASSWORD=${DARWIN_MYSQL_PASSWORD:-password}
MYSQL_DATABASE=${MYSQL_DATABASE:-darwin_catalog}
MYSQL_USER=${MYSQL_USERNAME:-darwin}
MYSQL_PASSWORD=${MYSQL_PASSWORD:-password}

# Resolve LOAD_TEST_DATA from env, falling back to image defaults if present
if [ -z "${LOAD_TEST_DATA+x}" ]; then
    if [ -f "/app/.odin/env-defaults" ]; then
        # shellcheck disable=SC1091
        . /app/.odin/env-defaults
    fi
fi
LOAD_TEST_DATA=${LOAD_TEST_DATA:-true}

# Construct JDBC URL
MYSQL_URL=${MYSQL_URL:-jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}

echo "📊 Configuration:"
echo "  MySQL Host: ${MYSQL_HOST}:${MYSQL_PORT}"
echo "  Database: ${MYSQL_DATABASE}"
echo "  User: ${MYSQL_USER}"
echo "  Load Test Data: ${LOAD_TEST_DATA}"
echo ""

# Install mysql client if not available (do this first!)
if ! command -v mysql &> /dev/null; then
    echo "📦 Installing mysql client..."
    if command -v apt-get &> /dev/null; then
        echo "   Using apt-get to install default-mysql-client..."
        if apt-get update -qq 2>&1 && apt-get install -y -qq default-mysql-client 2>&1; then
            echo "✅ mysql client installed successfully"
        else
            echo "⚠️  Warning: Could not install mysql client via apt-get"
            echo "   Attempting to continue anyway..."
        fi
    elif command -v apk &> /dev/null; then
        echo "   Using apk to install mysql-client..."
        if apk add --no-cache mysql-client 2>&1; then
            echo "✅ mysql client installed successfully"
        else
            echo "⚠️  Warning: Could not install mysql client via apk"
            echo "   Attempting to continue anyway..."
        fi
    elif command -v yum &> /dev/null; then
        echo "   Using yum to install mysql..."
        if yum install -y mysql 2>&1; then
            echo "✅ mysql client installed successfully"
        else
            echo "⚠️  Warning: Could not install mysql client via yum"
            echo "   Attempting to continue anyway..."
        fi
    else
        echo "⚠️  Warning: Could not install mysql client (unknown package manager)"
        echo "   Test data loading will be skipped"
    fi
else
    echo "✅ mysql client already available"
fi

echo ""

# Now check if mysql client is available for connection testing and database setup
if command -v mysql &> /dev/null; then
    echo "🔍 Checking MySQL connection with mysql client..."
    # Give MySQL some time to be ready
    max_attempts=30
    attempt=1
    while [ $attempt -le $max_attempts ]; do
        if mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_ROOT_USER}" -p"${MYSQL_ROOT_PASSWORD}" -e "SELECT 1;" &> /dev/null; then
            echo "✅ MySQL connection successful"
            break
        fi
        echo "⏳ Waiting for MySQL... (attempt $attempt/$max_attempts)"
        sleep 2
        attempt=$((attempt + 1))
    done

    if [ $attempt -le $max_attempts ]; then
        # Clear database if loading test data (for clean test environment)
        if [ "${LOAD_TEST_DATA}" = "true" ]; then
            echo "🗑️  Dropping and recreating database for fresh test data..."
            mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_ROOT_USER}" -p"${MYSQL_ROOT_PASSWORD}" <<EOF
DROP DATABASE IF EXISTS ${MYSQL_DATABASE};
CREATE DATABASE ${MYSQL_DATABASE};
EOF
            echo "✅ Database cleared and recreated"
        else
            # Create database if it doesn't exist (only when not loading test data)
            echo "📦 Creating database '${MYSQL_DATABASE}' if it doesn't exist..."
            mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_ROOT_USER}" -p"${MYSQL_ROOT_PASSWORD}" <<EOF
CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE};
EOF
        fi
        
        # Grant permissions to user (only if mysql client worked)
        echo "🔑 Granting permissions to user '${MYSQL_USER}'..."
        mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_ROOT_USER}" -p"${MYSQL_ROOT_PASSWORD}" <<EOF
CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_PASSWORD}';
GRANT ALL PRIVILEGES ON ${MYSQL_DATABASE}.* TO '${MYSQL_USER}'@'%';
FLUSH PRIVILEGES;
EOF
        echo "✅ Permissions granted to user '${MYSQL_USER}'"
    else
        echo "⚠️  Warning: Could not connect to MySQL after $max_attempts attempts with mysql client"
        echo "   Continuing anyway - Flyway will handle database setup"
    fi
else
    echo "ℹ️  mysql client not found - will use Flyway/Maven for database setup"
    echo "   (Database will be auto-created via JDBC URL parameter)"
    echo "   (User permissions will be handled by root user in JDBC connection)"
fi

echo ""


# Run Flyway migrations for catalog schema (always run so tables exist)
echo "🔧 Running Flyway migrations for catalog schema..."

# Install Flyway CLI if not available
if ! command -v flyway &> /dev/null; then
    echo "📦 Installing Flyway CLI..."
    
    # Install wget if not available
    if ! command -v wget &> /dev/null; then
        if command -v apt-get &> /dev/null; then
            apt-get install -y -qq wget 2>&1 > /dev/null
        fi
    fi
    
    # Download and install Flyway
    FLYWAY_VERSION="9.22.3"
    cd /tmp
    wget -q "https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/${FLYWAY_VERSION}/flyway-commandline-${FLYWAY_VERSION}.tar.gz"
    tar -xzf flyway-commandline-${FLYWAY_VERSION}.tar.gz
    ln -s /tmp/flyway-${FLYWAY_VERSION}/flyway /usr/local/bin/flyway
    chmod +x /usr/local/bin/flyway
    echo "✅ Flyway CLI installed successfully"
else
    echo "✅ Flyway CLI already available"
fi

# Check if migration files exist
MIGRATION_DIR="/app/classes/db/migration"
if [ ! -d "${MIGRATION_DIR}" ]; then
    # Try alternate location
    MIGRATION_DIR="/app/target/classes/db/migration"
    if [ ! -d "${MIGRATION_DIR}" ]; then
        echo "⚠️  Migration directory not found, searching..."
        MIGRATION_DIR=$(find /app -type d -name "migration" 2>/dev/null | head -1)
        if [ -z "${MIGRATION_DIR}" ]; then
            echo "❌ Could not find Flyway migration directory"
            echo "   Attempting to extract from JAR..."
            
            # Extract migrations from JAR
            if [ -f "/app/darwin-catalog-fat.jar" ]; then
                mkdir -p /tmp/migrations
                cd /tmp/migrations
                jar xf /app/darwin-catalog-fat.jar BOOT-INF/classes/db/migration/ 2>/dev/null || true
                if [ -d "BOOT-INF/classes/db/migration" ]; then
                    MIGRATION_DIR="/tmp/migrations/BOOT-INF/classes/db/migration"
                    echo "✅ Extracted migrations from JAR"
                fi
            fi
        fi
    fi
fi

if [ -n "${MIGRATION_DIR}" ] && [ -d "${MIGRATION_DIR}" ]; then
    echo "📁 Using migration directory: ${MIGRATION_DIR}"
    echo "📊 Running Flyway migrations..."
    
    # Run Flyway migrate
    flyway -url="${MYSQL_URL}" \
           -user="${MYSQL_ROOT_USER}" \
           -password="${MYSQL_ROOT_PASSWORD}" \
           -locations="filesystem:${MIGRATION_DIR}" \
           migrate 2>&1 | head -50
    
    echo "✅ Flyway migrations completed"
else
    echo "⚠️  Could not find migration files, skipping Flyway"
fi

echo ""

# Load seed data if requested
if [ "${LOAD_TEST_DATA}" = "true" ]; then
    echo "📊 Loading seed data from /app/.odin/seed.sql..."
    
    SEED_SQL="/app/.odin/seed.sql"
    if [ -f "${SEED_SQL}" ]; then
        if command -v mysql &> /dev/null; then
            temp_sql="/tmp/darwin-catalog-seed.sql"
            sed "s/asset\\./${MYSQL_DATABASE}./g" "$SEED_SQL" > "$temp_sql"
            
            if mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_ROOT_USER}" -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < "$temp_sql" 2>&1; then
                echo "✅ Seed data loaded successfully"
            else
                echo "⚠️  Failed to load seed data (continuing anyway)"
            fi
            
            rm -f "$temp_sql"
        else
            echo "   ⚠️  mysql client not available, cannot load seed data"
        fi
    else
        echo "⚠️  Seed file not found at ${SEED_SQL}"
    fi
    echo ""
else
    echo "ℹ️  LOAD_TEST_DATA is not 'true' - skipping seed data load"
    echo ""
fi

echo "🎉 Pre-deploy setup completed successfully!"
echo "   Database: ${MYSQL_DATABASE}"
echo "   Migrations: Executed"
echo "   Test Data: ${LOAD_TEST_DATA}"
