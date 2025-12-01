#!/usr/bin/env bash
set -e

# Check database connectivity and setup
echo ""
echo "Checking MySQL connectivity and setting up database..."

# Get connection parameters
# For darwin-ml-serve, we use simpler environment variable names
echo "  MYSQL_HOST: ${MYSQL_HOST}"
echo "  MYSQL_USERNAME: ${MYSQL_USERNAME}"
echo "  MYSQL_PASSWORD: ${MYSQL_PASSWORD:+***set***}"
echo "  MYSQL_DATABASE: ${MYSQL_DATABASE}"

HOST="${MYSQL_HOST:-localhost}"
USER="${MYSQL_USERNAME:-root}"
PASSWORD="${MYSQL_PASSWORD:-}"
DATABASE="${MYSQL_DATABASE:-mlp_serve}"

echo "Connecting to MySQL at ${HOST}..."
echo "  Database: ${DATABASE}"
echo "  User: ${USER}"

# Try to connect using mysql CLI (available in the image)
max_retries=5
attempt=1
CONNECTED=false

while [ $attempt -le $max_retries ]; do
  if [ -z "$PASSWORD" ]; then
    # Empty password - omit the -p flag entirely
    if mysql -h"${HOST}" -u"${USER}" -e "SELECT 1;" >/dev/null 2>&1; then
      echo "✅ MySQL connection successful to ${HOST} as ${USER}"
      CONNECTED=true
      break
    fi
  else
    # Non-empty password
    if mysql -h"${HOST}" -u"${USER}" -p"${PASSWORD}" -e "SELECT 1;" >/dev/null 2>&1; then
      echo "✅ MySQL connection successful to ${HOST} as ${USER}"
      CONNECTED=true
      break
    fi
  fi
  
  if [ $attempt -lt $max_retries ]; then
    echo "  Attempt ${attempt}/${max_retries} failed, retrying in 5s..."
    sleep 5
  else
    echo "❌ MySQL connection failed after ${max_retries} attempts"
    echo "   Check if MySQL is accessible at ${HOST} and credentials are correct"
    exit 1
  fi
  attempt=$((attempt + 1))
done

if [ "$CONNECTED" = false ]; then
  exit 1
fi

# Create database if it doesn't exist
echo ""
echo "📦 Setting up database: ${DATABASE}"
if [ -z "$PASSWORD" ]; then
  mysql -h"${HOST}" -u"${USER}" -e "CREATE DATABASE IF NOT EXISTS \`${DATABASE}\`;" 2>/dev/null && \
    echo "✅ Database '${DATABASE}' is ready" || \
    echo "⚠️  Could not create database (may already exist or insufficient permissions)"
else
  mysql -h"${HOST}" -u"${USER}" -p"${PASSWORD}" -e "CREATE DATABASE IF NOT EXISTS \`${DATABASE}\`;" 2>/dev/null && \
    echo "✅ Database '${DATABASE}' is ready" || \
    echo "⚠️  Could not create database (may already exist or insufficient permissions)"
fi

# Generate tables using Tortoise ORM if Python environment is available
echo ""
echo "📋 Setting up database tables..."

# Check if we're in a container with the application
if [ -d "$BASE_DIR" ] && [ -d "$BASE_DIR/model" ]; then
  echo "  Using application's Python environment to generate schemas..."
  echo "  BASE_DIR: $BASE_DIR"

  # Change to base directory
  cd "$BASE_DIR" || exit 1

  # Use Python to generate tables via Tortoise ORM (packages installed to system python3)
  python3 << 'PYTHON_SCRIPT'
import asyncio
import sys
import os

# Get BASE_DIR from environment or default to /app
BASE_DIR = os.getenv('BASE_DIR', '/app')

# Add app directories to path
sys.path.insert(0, BASE_DIR)
sys.path.insert(0, os.path.join(BASE_DIR, 'model/src'))
sys.path.insert(0, os.path.join(BASE_DIR, 'core/src'))
sys.path.insert(0, os.path.join(BASE_DIR, 'app_layer/src'))

try:
    from tortoise import Tortoise
    from tortoise.exceptions import DBConnectionError
    
    print(f"  BASE_DIR: {BASE_DIR}")
    print(f"  Python sys.path: {sys.path[:3]}")

    # Get connection parameters from environment
    host = os.getenv('MYSQL_HOST', 'localhost')
    user = os.getenv('MYSQL_USERNAME', 'root')
    password = os.getenv('MYSQL_PASSWORD', '')
    database = os.getenv('MYSQL_DATABASE', 'mlp_serve')
    
    print(f"  Database config: {user}@{host}/{database}")

    # Construct db_url - handle empty password correctly
    if password:
        db_url = f'mysql://{user}:{password}@{host}/{database}'
    else:
        db_url = f'mysql://{user}@{host}/{database}'

    async def init_db():
        try:
            # Import models to ensure they're registered
            print("  Importing serve_model...")
            import serve_model
            print(f"  serve_model imported from: {serve_model.__file__}")

            print(f"  Initializing Tortoise ORM...")
            await Tortoise.init(
                db_url=db_url,
                modules={'models': ['serve_model.image_builder']}
            )
            print(f"  ✅ Connected to database: {database}")

            # Generate schemas (create tables)
            print("  Generating database schemas...")
            await Tortoise.generate_schemas(safe=True)
            print("  ✅ Database table 'darwin_image_builder' created/verified successfully")

            await Tortoise.close_connections()
        except DBConnectionError as e:
            print(f"  ❌ Database connection error: {e}")
            import traceback
            traceback.print_exc()
            sys.exit(1)  # Fail pre-deploy on connection errors
        except Exception as e:
            print(f"  ❌ Error setting up table: {e}")
            import traceback
            traceback.print_exc()
            sys.exit(1)  # Fail pre-deploy on errors

    asyncio.run(init_db())

except ImportError as e:
    print(f"  ❌ Import error: {e}")
    import traceback
    traceback.print_exc()
    print(f"  Python path: {sys.path}")
    sys.exit(1)  # Fail pre-deploy on import errors
except Exception as e:
    print(f"  ❌ Unexpected error: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)  # Fail pre-deploy on unexpected errors

PYTHON_SCRIPT

  PYTHON_EXIT=$?
  if [ $PYTHON_EXIT -eq 0 ]; then
    echo "✅ Database setup completed"
  else
    echo "⚠️  Table generation had issues, but continuing..."
  fi
else
  echo "  ⚠️  Application directory not found at $BASE_DIR"
  echo "  Table will be created by the application on startup (Tortoise ORM generate_schemas=True)"
fi

echo ""
echo "✅ Pre-deploy database setup completed"
