#!/bin/bash

echo "🔧 Setting up Kafka topics for Chronos..."

# Kafka configuration
KAFKA_HOST="${DARWIN_KAFKA_HOST:-darwin-kafka}"
KAFKA_PORT="9092"
KAFKA_BROKER="$KAFKA_HOST:$KAFKA_PORT"

echo "📡 Kafka Broker: $KAFKA_BROKER"

# Check if Kafka is available
echo "⏳ Checking if Kafka is available..."
max_attempts=30
attempt=1

while [ $attempt -le $max_attempts ]; do
    if timeout 2 bash -c "cat < /dev/null > /dev/tcp/$KAFKA_HOST/$KAFKA_PORT" 2>/dev/null; then
        echo "✅ Kafka is available!"
        break
    fi

    if [ $attempt -eq $max_attempts ]; then
        echo "❌ Kafka not available after $max_attempts attempts"
        exit 1
    fi

    echo "   Attempt $attempt/$max_attempts - waiting for Kafka..."
    sleep 2
    attempt=$((attempt + 1))
done

# List of topics needed for Chronos
TOPICS=(
    "raw-events"
    "processed-events"
    "dlq-events"
)

echo "📝 Creating Kafka topics..."

# Use kafka-topics command (available in Kafka container)
for TOPIC in "${TOPICS[@]}"; do
    echo "   Creating topic: $TOPIC"

    # This assumes kafka-topics.sh is available in the PATH or via the Kafka pod
    # Alternative: use kubectl exec into the kafka pod
    kubectl exec -n darwin darwin-kafka-0 -- \
        kafka-topics --create \
        --bootstrap-server localhost:9092 \
        --topic $TOPIC \
        --partitions 3 \
        --replication-factor 1 \
        --if-not-exists \
        2>/dev/null || echo "   (Topic may already exist)"
done

echo ""
echo "✅ Kafka topics setup complete!"
echo ""
echo "📊 Configured Topics:"
for TOPIC in "${TOPICS[@]}"; do
    echo "   - $TOPIC"
done