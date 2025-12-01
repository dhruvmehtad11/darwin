# Transformers

Transformers convert raw events from various sources into structured processed events with entities and relationships.

## Overview

A transformer takes a raw event and produces:
- **Processed Events** - Enriched events with type, severity, and structured data
- **Entities** - Domain objects (clusters, users, workspaces, etc.)
- **Links** - Relationships between entities

## Transformer Types

### 1. Python Transformers

Custom Python code for complex transformation logic.

**Location:** `python_transformers/`

**Base Class:** `BasePythonTransformer`

**Example:**
```python
from src.transformers.base_python_transformer import BasePythonTransformer
from src.dto.schema.base_transformers import TransformerOutput
from src.dto.schema import RawEvent
import json

class MyTransformer(BasePythonTransformer):
    async def is_transformer_applicable(self, event_data: RawEvent) -> bool:
        """Check if transformer should process this event"""
        return True
    
    async def transform(self, event_data: RawEvent) -> TransformerOutput:
        """Transform raw event to processed events, entities, and links"""
        raw_data = json.loads(event_data.EventData)
        
        return TransformerOutput(
            processed_events=[{
                'EventType': 'MY_EVENT',
                'EntityID': raw_data['id'],
                'EventData': raw_data,
                'Severity': 'INFO',
                'Message': 'Event processed'
            }],
            entities=[{
                'EntityID': raw_data['id'],
                'EntityType': 'MyEntity'
            }],
            links=[]
        )
```

**Register:**
```bash
curl -X POST "http://localhost/chronos/api/v1/transformers/" \
  -H "Content-Type: application/json" \
  -d '{
    "TransformerName": "my-transformer",
    "TransformerType": "PythonTransformer",
    "SourceID": 1,
    "ScriptPath": "src.transformers.python_transformers.my_transformer",
    "IsActive": true
  }'
```

### 2. JSON Transformers

JSONPath-based transformations for simple field mappings.

**Use Cases:**
- Field extraction
- Simple mappings
- No complex logic required

**Example:**
```json
{
  "TransformerName": "simple-mapper",
  "TransformerType": "JSONTransformer",
  "SourceID": 1,
  "TransformerScript": {
    "eventType": "$.event_type",
    "entityId": "$.resource_id",
    "severity": "INFO"
  },
  "IsActive": true
}
```

## Transformer Registry

The registry manages all transformers and provides a unified interface for retrieval.

**File:** `registry.py`

**Usage:**
```python
from src.transformers.registry import transformer_registry

# Get transformer instance
transformer = await transformer_registry.get_transformer(transformer_obj)

# Apply transformer
result = await transformer.apply(raw_event)
```

## Available Python Transformers

### 1. Compute Events Transformer
**File:** `compute_app_layer_transformer.py`  
**Source:** compute-service  
**Events:** CLUSTER_CREATED, CLUSTER_TERMINATED, etc.

### 2. Workflow Transformer
**File:** `workflow_transformer.py`  
**Source:** workflow-service  
**Events:** WORKFLOW_STARTED, WORKFLOW_COMPLETED, etc.

### 3. AWS Transformer
**File:** `aws_transformer.py`  
**Source:** aws  
**Events:** AWS CloudTrail events

### 4. K8s Transformer
**File:** `k8s_transformer.py`  
**Source:** kubernetes  
**Events:** Pod, Deployment events

## Creating a New Transformer

### Step 1: Create Transformer Class

Create file in `python_transformers/`:

```python
# python_transformers/my_new_transformer.py
from src.transformers.base_python_transformer import BasePythonTransformer
from src.dto.schema.base_transformers import TransformerOutput
from src.dto.schema import RawEvent
import json
from loguru import logger

class MyNewTransformer(BasePythonTransformer):
    async def is_transformer_applicable(self, event_data: RawEvent) -> bool:
        """Determine if this transformer should process the event"""
        try:
            data = json.loads(event_data.EventData)
            return 'my_field' in data
        except:
            return False
    
    async def transform(self, event_data: RawEvent) -> TransformerOutput:
        """Transform the raw event"""
        try:
            raw_data = json.loads(event_data.EventData)
            
            return TransformerOutput(
                processed_events=[{
                    'EventType': raw_data.get('event_type', 'UNKNOWN'),
                    'EntityID': raw_data['entity_id'],
                    'EventData': raw_data,
                    'Severity': self._determine_severity(raw_data),
                    'Message': raw_data.get('message')
                }],
                entities=[{
                    'EntityID': raw_data['entity_id'],
                    'EntityType': 'MyEntityType'
                }],
                links=self._extract_links(raw_data)
            )
        except Exception as e:
            logger.exception(f"Error transforming event: {e}")
            return None
    
    def _determine_severity(self, data: dict) -> str:
        """Helper to determine event severity"""
        if data.get('error'):
            return 'ERROR'
        return 'INFO'
    
    def _extract_links(self, data: dict) -> list:
        """Helper to extract entity relationships"""
        links = []
        if 'parent_id' in data:
            links.append({
                'SourceEntityID': data['entity_id'],
                'DestinationEntityID': data['parent_id']
            })
        return links
```

### Step 2: Register Transformer

First, create a source:
```bash
curl -X POST "http://localhost/chronos/api/v1/sources/" \
  -H "Content-Type: application/json" \
  -d '{
    "SourceName": "my-service",
    "EventFormatType": "application/json"
  }'
```

Then register the transformer:
```bash
curl -X POST "http://localhost/chronos/api/v1/transformers/" \
  -H "Content-Type: application/json" \
  -d '{
    "TransformerName": "my-new-transformer",
    "TransformerType": "PythonTransformer",
    "SourceID": 1,
    "ScriptPath": "src.transformers.python_transformers.my_new_transformer",
    "IsActive": true
  }'
```

### Step 3: Test Transformer

Send a test event:
```bash
curl -X POST "http://localhost/chronos/api/v1/event/test" \
  -H "x-event-source: my-service" \
  -H "Content-Type: application/json" \
  -d '{
    "entity_id": "test-123",
    "event_type": "TEST_EVENT",
    "message": "Test message"
  }'
```

## Transformer Output Schema

```python
TransformerOutput(
    processed_events=[{
        'EventType': str,          # Event type name
        'EntityID': str,           # Primary entity identifier
        'EventData': dict,         # Full event data (JSON)
        'Severity': str,           # INFO, WARNING, ERROR, CRITICAL
        'Message': str             # Human-readable message
    }],
    entities=[{
        'EntityID': str,           # Unique entity identifier
        'EntityType': str          # Entity type (Cluster, User, etc.)
    }],
    links=[{
        'SourceEntityID': str,     # Source entity
        'DestinationEntityID': str # Destination entity
    }]
)
```

### List Transformers
```bash
# All transformers
curl "http://localhost/chronos/api/v1/transformers/"

# Transformers for specific source
curl "http://localhost/chronos/api/v1/transformers/source/1"
```

