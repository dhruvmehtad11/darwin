#!/bin/bash
# Simple way to run artifact-builder locally

cd "$(dirname "$0")"

echo "======================================"
echo "Darwin ML Serve - Simple Startup"
echo "======================================"
echo ""

# Check if .env exists
if [ ! -f ".env" ]; then
    echo "Creating .env from env.example..."
    cp env.example .env
fi

# Load environment variables
export $(cat .env | grep -v '^#' | xargs)

# Check MySQL is running
if ! docker ps | grep -q mysql-local; then
    echo "⚠ MySQL container not running. Start it with:"
    echo "  docker run --name mysql-local -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=mlp_serve -p 3306:3306 -d mysql:8.0"
    echo ""
fi

# Ensure logs directory exists
mkdir -p logs

echo ""
echo "Environment: $ENV"
echo "Logs directory: $LOG_FILE_ROOT"
echo "Database: $MYSQL_DATABASE"
echo "Port: 8000"
echo ""
echo "Starting server with background worker..."
echo "API docs: http://localhost:8000/docs"
echo ""

# Activate virtual environment if it exists
if [ -d ".venv" ]; then
    source .venv/bin/activate
fi

# Start the server (this will include background worker due to __main__ block)
cd app_layer/src
python -m serve_app_layer.main
