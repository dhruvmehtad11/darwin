#!/bin/bash

echo "🔧 Setting up SQS queues in LocalStack for Chronos..."

# LocalStack endpoint
LOCALSTACK_ENDPOINT="${AWS_ENDPOINT_OVERRIDE:-http://darwin-localstack:4566}"
AWS_REGION="${AWS_REGION:-us-east-1}"

echo "📡 LocalStack Endpoint: $LOCALSTACK_ENDPOINT"
echo "🌍 AWS Region: $AWS_REGION"

# Check if LocalStack is available
echo "⏳ Checking if LocalStack is available..."
if timeout 5 bash -c "curl -s $LOCALSTACK_ENDPOINT/_localstack/health" >/dev/null 2>&1; then
    echo "✅ LocalStack is available!"
else
    echo "⚠️  LocalStack not available yet"
    echo "   Queues will need to be created manually or on next startup"
    exit 0
fi

# List of SQS queues needed for Chronos
QUEUES=(
    "chronos-raw-events-queue"
    "chronos-processed-events-queue"
    "chronos-dlq-queue"
)

echo "📝 Creating SQS queues..."

for QUEUE in "${QUEUES[@]}"; do
    echo "   Creating queue: $QUEUE"

    # Create queue via curl
    RESPONSE=$(curl -s -X POST "$LOCALSTACK_ENDPOINT" \
        -d "Action=CreateQueue" \
        -d "QueueName=$QUEUE" \
        -d "Version=2012-11-05")

    # Extract QueueUrl from response
    QUEUE_URL=$(echo "$RESPONSE" | grep -oPm1 "(?<=<QueueUrl>)[^<]+")

    if [ -n "$QUEUE_URL" ]; then
        echo "   ✅ Queue $QUEUE created: $QUEUE_URL"

        # Set queue attributes via curl
        curl -s -X POST "$LOCALSTACK_ENDPOINT" \
            -d "Action=SetQueueAttributes" \
            -d "QueueUrl=$QUEUE_URL" \
            -d "Attribute.1.Name=VisibilityTimeout" \
            -d "Attribute.1.Value=30" \
            -d "Attribute.2.Name=MessageRetentionPeriod" \
            -d "Attribute.2.Value=345600" \
            -d "Version=2012-11-05" >/dev/null

        echo "   ✅ Queue $QUEUE configured"
    else
        echo "   ⚠️ Failed to create queue $QUEUE (may already exist)"
    fi
done

echo ""
echo "✅ SQS setup complete for Chronos!"
echo ""
echo "📊 Configured Queues:"
for QUEUE in "${QUEUES[@]}"; do
    echo "   - $QUEUE"
done
echo ""
echo "🔗 LocalStack Endpoint: $LOCALSTACK_ENDPOINT"
