#!/bin/bash

# Script to completely reset the database (removes all data)
# Use this if you want a fresh start

echo "Stopping containers..."
docker compose down

echo "Removing MySQL volume..."
docker volume rm darwin-catalog_mysql_data 2>/dev/null || echo "Volume already removed or doesn't exist"

echo "Starting fresh..."
docker compose up -d

echo "Waiting for MySQL to be ready..."
sleep 15

echo "Database reset complete. The application will now run migrations from scratch."

