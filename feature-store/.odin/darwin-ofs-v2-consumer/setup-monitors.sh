#!/usr/bin/env bash

# Script to setup JMX monitoring for Kafka consumer in Datadog
# This script creates the necessary configuration and restarts the Datadog agent

set -e

echo "Setting up JMX monitoring for Kafka consumer in Datadog..."

# Create the configuration file using a reliable method for Debian
CONFIG_FILE="/etc/datadog-agent/conf.d/jmx.d/conf.yaml"
CONFIG_CONTENT="init_config:
  is_jmx: true
  collect_default_metrics: true
  conf:
  - include:
      domain: kafka.consumer
instances:
  - host: localhost
    port: 1099"

# Write the content to the file using echo and sudo tee
echo "$CONFIG_CONTENT" | sudo tee "$CONFIG_FILE" > /dev/null

# Verify the file was created
if [ -f "${CONFIG_FILE}" ]; then
  echo "JMX configuration file created successfully at ${CONFIG_FILE}"
  sudo cat "${CONFIG_FILE}"  # Display the file contents to verify
else
  echo "ERROR: Failed to create JMX configuration file at ${CONFIG_FILE}"
  exit 1
fi

# Restart the Datadog agent
echo "Restarting Datadog agent..."
sudo service datadog-agent restart

