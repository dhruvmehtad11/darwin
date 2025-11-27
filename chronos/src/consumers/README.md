# Consumers

Consumers process raw events from message queues (SQS or Kafka) and apply transformers to generate processed events.

## Overview

The consumer service runs continuously, polling the queue for new raw event IDs, retrieving the raw events from the database, and processing them through registered transformers.

## Supported Queues

### 1. SQS (AWS Simple Queue Service)

### 2. Kafka

## Consumer Flow

```
┌─────────────┐    ┌──────────────┐    ┌─────────────┐    ┌──────────────┐
│ Queue       │───▶│ Consumer     │───▶│ Event       │───▶│ Database     │
│ (SQS/Kafka) │    │ Worker       │    │ Processor   │    │ (Processed)  │
└─────────────┘    └──────────────┘    └─────────────┘    └──────────────┘
                           │                   │
                           │                   ▼
                           │            ┌─────────────┐
                           └───────────▶│ Transformer │
                                        │ Registry    │
                                        └─────────────┘
```

### Steps:

1. **Poll Queue** - Consumer polls for messages containing raw event IDs
2. **Retrieve Event** - Fetch raw event from `raw_events` table using the ID
3. **Lookup Source** - Find source configuration by name and content type
4. **Get Transformers** - Retrieve all active transformers for the source
5. **Apply Transformers** - Execute each transformer on the raw event
6. **Save Results** - Persist processed events, entities, and links


### Health Check

```bash
curl http://localhost/chronos-consumer/healthcheck
```