#!/bin/bash

# Configuration
HOST="127.0.0.1"
PORT=3306
USER="root"
PASSWORD="password"
TIMEOUT=5  # Timeout in seconds between checks
MAX_WAIT=100  # Max wait time in seconds

echo "Waiting for MySQL to be up and running..."

START_TIME=$SECONDS

# Loop until MySQL is available or until max wait time is reached
until mysql -h "$HOST" -P "$PORT" -u "$USER" -p"$PASSWORD" -e "SELECT 1" "$DATABASE" &>/dev/null; do
  if (( SECONDS - START_TIME > MAX_WAIT )); then
    echo "Timeout reached! MySQL is not up after $MAX_WAIT seconds."
    exit 1
  fi
  echo "Waiting for MySQL server to be ready..."
  sleep $TIMEOUT
done

echo "MySQL is up and running!"



#Create database 'main'
mysql -h 127.0.0.1 -P 3306 -u root -ppassword <<<"CREATE DATABASE IF NOT EXISTS main"

#create DB Migrations
mysql -h 127.0.0.1 -P 3306 -u root -ppassword < resources/db/mysql/migrations/20230627145332_CreateTables.sql