#!/bin/bash

set -e

echo "Starting Darwin Catalog..."

# Check prerequisites
command -v docker >/dev/null 2>&1 || { echo "ERROR: Docker is required but not installed. Aborting." >&2; exit 1; }

# Detect docker compose command (docker compose or docker-compose)
if docker compose version >/dev/null 2>&1; then
    DOCKER_COMPOSE="docker compose"
elif command -v docker-compose >/dev/null 2>&1; then
    DOCKER_COMPOSE="docker-compose"
else
    echo "ERROR: Docker Compose is required but not installed. Aborting." >&2
    exit 1
fi

# Check if ports are available
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 || nc -z localhost $port 2>/dev/null; then
        echo "WARNING: Port $port is already in use"
        return 1
    fi
    return 0
}

PORTS=(3306 8080)
PORT_IN_USE=false

for port in "${PORTS[@]}"; do
    if ! check_port $port; then
        PORT_IN_USE=true
    fi
done

if [ "$PORT_IN_USE" = true ]; then
    echo "ERROR: Some required ports are already in use. Please free them and try again."
    exit 1
fi

# Build and start services
echo "Building Docker image..."
$DOCKER_COMPOSE build --quiet

echo "Starting services..."
$DOCKER_COMPOSE up -d --quiet-pull

echo "Waiting for services to be ready..."
sleep 5

# Wait for MySQL to be ready
max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if $DOCKER_COMPOSE exec -T mysql mysqladmin ping -h localhost -u root -prootpassword --silent 2>/dev/null; then
        break
    fi
    attempt=$((attempt + 1))
    sleep 2
done

if [ $attempt -eq $max_attempts ]; then
    echo "ERROR: MySQL failed to start within expected time"
    $DOCKER_COMPOSE logs --tail=50 mysql
    exit 1
fi

# Grant permissions to darwin-catalog user from any host (for Docker network connections)
echo "Configuring MySQL user permissions..."
$DOCKER_COMPOSE exec -T mysql mysql -u root -prootpassword -e "CREATE USER IF NOT EXISTS 'darwin-catalog'@'%' IDENTIFIED BY 'darwin-catalog'; GRANT ALL PRIVILEGES ON darwin_catalog.* TO 'darwin-catalog'@'%'; FLUSH PRIVILEGES;" 2>/dev/null || true

# Wait for application to be ready
max_attempts=60
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if curl -s http://localhost:8080/healthcheck >/dev/null 2>&1; then
        break
    fi
    attempt=$((attempt + 1))
    sleep 2
done

if [ $attempt -eq $max_attempts ]; then
    echo "WARNING: Application may still be starting"
    echo "Check logs: $DOCKER_COMPOSE logs darwin-catalog"
    exit 1
fi

echo "Darwin Catalog is running!"
echo ""
echo "Service URLs:"
echo "  - API: http://localhost:8080"
echo "  - API Documentation: http://localhost:8080/swagger-ui.html"
echo ""
echo "Useful commands:"
echo "  - View logs: $DOCKER_COMPOSE logs -f darwin-catalog"
echo "  - Stop services: $DOCKER_COMPOSE down"
echo "  - Clean up: $DOCKER_COMPOSE down -v"
