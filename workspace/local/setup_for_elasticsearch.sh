#!/bin/bash

# Configuration
TIMEOUT=5  # Timeout in seconds between checks
MAX_WAIT=100  # Max wait time in seconds

echo "Waiting for Elasticsearch to be up and running..."

START_TIME=$SECONDS

# Loop until ES is available or until max wait time is reached
until curl -X GET 'localhost:9200'; do
  if (( SECONDS - START_TIME > MAX_WAIT )); then
    echo "Timeout reached! ES is not up after $MAX_WAIT seconds."
    exit 1
  fi
  echo "Waiting for ES server to be ready..."
  sleep $TIMEOUT
done

echo "ES is up and running!"

#make credentials index in ES
curl -X PUT "localhost:9200/credentials" -H 'Content-Type: application/json' -d'
{
  "mappings": {
    "properties": {
      "username": { "type": "text" },
      "password": { "type": "text" }
    }
  }
}
'

#make a doc with username as 'dsuser' in credentials index of ES
curl -X POST "localhost:9200/credentials/_doc/1"  -H 'Content-Type: application/json' -d'
{
  "username": "dsuser",
  "password": ""
}
'
