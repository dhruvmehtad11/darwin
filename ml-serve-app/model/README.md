# Darwin ML Serve - Database Models

This package contains all database models and schemas for the Darwin ML Serve platform using **Tortoise ORM**.

## Overview

The model package defines the complete database schema for managing ML model deployments, including:
- **User Management**: Authentication and user profiles
- **Serve Lifecycle**: ML service definitions and configurations  
- **Artifact Management**: Build jobs and deployment artifacts
- **Environment Management**: Multi-environment deployment support
- **Deployment Tracking**: Active deployments and lifecycle management

## Database Architecture

### Technology Stack
- **ORM**: Tortoise ORM (async Python ORM)
- **Database**: MySQL 8.0+
- **Schema Management**: Automatic generation from model definitions
- **Migrations**: Model-first approach (no manual SQL migrations)

### Database Configuration

The database connection is configured through:
- **Config Files**: `resources/config/mysql/connection-*.conf`
- **Environment Variables**: See root README.md for database environment variables
- **Automatic Initialization**: Schema created on application startup

## Database Schema

### Core Tables

#### 1. **users** - User Management
```python
class User(models.Model):
    user_id = IntField(pk=True)
    username = CharField(max_length=50, unique=True)
    token = CharField(max_length=255)
    created_at = DatetimeField(auto_now_add=True)
    updated_at = DatetimeField(auto_now=True)
```

**Purpose**: Stores user authentication and profile information

#### 2. **serves** - ML Service Definitions
```python
class Serve(models.Model):
    id = IntField(pk=True)
    name = CharField(max_length=50, unique=True)
    type = CharField(max_length=50)  # 'api' or 'workflow'
    description = TextField(null=True)
    space = TextField(null=False)
    created_by = ForeignKeyField('models.User')
    created_at = DatetimeField(auto_now_add=True)
    updated_at = DatetimeField(auto_now=True)
```

**Purpose**: Defines ML services that can be deployed (API serves or Workflow serves)

#### 3. **environments** - Deployment Environments
```python
class Environment(models.Model):
    id = IntField(pk=True)
    name = CharField(max_length=255)
    env_configs = JSONField(null=False)  # Stores all environment configuration such as Cluster, domain, security configs etc. as JSON
    is_protected = BooleanField(default=False)
    created_at = DatetimeField(auto_now_add=True)
    updated_at = DatetimeField(auto_now=True)
```

**Purpose**: Stores deployment environments (local, prod, custom) with their configurations

**JSON Structure (`env_configs`):**
```json
{
  "domain_suffix": ".mycompany.com",
  "cluster_name": "prod-k8s-cluster",
  "namespace": "ml-serve",
  "security_group": "sg-12345, sg-67890",
  "subnets": "subnet-abc123, subnet-def456",
  "ft_redis_url": "redis://prod-redis:6379",
  "workflow_url": "http://workflow-service:8080"
}
```

#### 4. **artifact_builder_job** - Build Job Tracking
```python
class ArtifactBuilderJob(models.Model):
    id = IntField(pk=True)
    serve = ForeignKeyField('models.Serve')
    version = CharField(max_length=50)
    github_repo_url = TextField(null=False)
    task_id = TextField(null=True)
    status = CharField(max_length=50, default='PENDING')  # PENDING/SUCCESSFUL/FAILED
    created_at = DatetimeField(auto_now_add=True)
    last_picked_at = DatetimeField()
    status_last_updated_at = DatetimeField(null=True)
    created_by = ForeignKeyField('models.User')
```

**Purpose**: Tracks artifact building jobs (Docker image creation from source code)

#### 5. **artifacts** - Deployment Artifacts
```python
class Artifact(models.Model):
    id = IntField(pk=True)
    serve = ForeignKeyField('models.Serve')
    version = CharField(max_length=50)
    github_repo_url = TextField(null=False)
    image_url = TextField(null=True)  # Docker image URL
    branch = CharField(max_length=50, null=True)
    file_path = CharField(max_length=255, null=True)
    artifact_job = ForeignKeyField('models.ArtifactBuilderJob', null=True)
    created_at = DatetimeField(auto_now_add=True)
    created_by = ForeignKeyField('models.User')
```

**Purpose**: Stores built artifacts (Docker images) for each serve version
**Constraints**: Unique combination of serve + version

#### 6. **deployments** - Deployment Records
```python
class Deployment(models.Model):
    id = IntField(pk=True)
    serve = ForeignKeyField('models.Serve')
    artifact = ForeignKeyField('models.Artifact')
    environment = ForeignKeyField('models.Environment')
    created_by = ForeignKeyField('models.User')
    created_at = DatetimeField(auto_now_add=True)
    status = CharField(max_length=20, default='ACTIVE')  # ACTIVE/ENDED
    ended_at = DatetimeField(null=True)
```

**Purpose**: Records all deployments of serves to environments with lifecycle tracking

#### 7. **active_deployments** - Current Active Deployments
```python
class ActiveDeployment(models.Model):
    id = IntField(pk=True)
    deployment = ForeignKeyField('models.Deployment')
    previous_deployment = ForeignKeyField('models.Deployment', null=True)
    environment = ForeignKeyField('models.Environment')
    serve = ForeignKeyField('models.Serve')
    created_at = DatetimeField(auto_now_add=True)
    updated_at = DatetimeField(auto_now=True)
```

**Purpose**: Tracks which deployment is currently active for each serve in each environment

### Configuration Tables

#### 8. **APIServeInfraConfig** - API Serve Infrastructure Configuration
```python
class APIServeInfraConfig(models.Model):
    # Infrastructure settings for FastAPI/Ray serves
    # CPU, memory, min/max replicas, node capacity
    # Kubernetes HPA configurations
```

**Purpose**: Defines infrastructure configuration for API serves
**Key Settings:**
- Resource limits (CPU cores, memory)
- Horizontal Pod Autoscaler (min/max replicas)
- Node capacity type (on-demand/spot)
- Additional host configurations

#### 9. **WorkflowServeInfraConfig** - Workflow Serve Infrastructure Configuration  
```python
class WorkflowServeInfraConfig(models.Model):
    # Infrastructure settings for workflow serves
    # Job cluster configurations (head/worker nodes)
    # Scheduling configurations
```

**Purpose**: Defines infrastructure configuration for workflow serves
**Key Settings:**
- Head node configuration (CPU, memory, node type)
- Worker node configurations (pods, resources)
- Workflow scheduling (cron expressions)

## Data Relationships

```
User (1) ──→ (N) Serve ──→ (N) Artifact ──→ (N) Deployment
                │              │              │
                │              │              └──→ (1) Environment
                │              │
                │              └──→ (1) ArtifactBuilderJob
                │
                └──→ (N) ActiveDeployment ──→ (1) Environment
```

### Key Relationships:
- **User → Serves**: Users can create multiple serves
- **Serve → Artifacts**: Each serve can have multiple versioned artifacts
- **Artifact → Deployments**: Each artifact can be deployed multiple times
- **Environment ↔ Deployments**: Many-to-many relationship through deployments
- **ActiveDeployment**: Tracks current active deployment per serve per environment

## Enums and Constants

### ServeType
```python
class ServeType(Enum):
    API = "api"        # FastAPI/Ray Serve deployments
    WORKFLOW = "workflow"  # Workflow-based deployments
```

### JobStatus
```python
class JobStatus(Enum):
    PENDING = "PENDING"
    SUCCESSFUL = "SUCCESSFUL"
    FAILED = "FAILED"
```

### DeploymentStatus
```python
class DeploymentStatus(Enum):
    ACTIVE = "ACTIVE"
    ENDED = "ENDED"
```

## Model Features

### Automatic Timestamps
- **created_at**: Automatically set on record creation
- **updated_at**: Automatically updated on record modification

### Foreign Key Relationships
- **Cascade Deletes**: Proper cleanup when parent records are deleted
- **Related Names**: Easy reverse relationship access

### JSON Fields
- **Environment Configurations**: Flexible storage for environment-specific settings
- **Dynamic Properties**: Computed properties for easy access to nested JSON data

## Database Initialization

The database schema is automatically created when the application starts:

1. **Connection Setup**: MySQL client reads configuration from `resources/config/mysql/`
2. **Model Registration**: Tortoise ORM registers all models from this package
3. **Schema Generation**: `generate_schemas=True` creates tables automatically
4. **Relationship Setup**: Foreign keys and constraints are established

## Development Guidelines

### Adding New Models
1. Create new model class inheriting from `tortoise.models.Model`
2. Add to `__init__.py` exports
3. Restart application to auto-generate schema

### Modifying Existing Models
1. Update model class definition
2. Restart application (schema will be updated automatically)
3. **Note**: Data migration may be needed for production

### Best Practices
- Use descriptive field names
- Add proper foreign key relationships
- Include `created_at`/`updated_at` for audit trails
- Use enums for status fields
- Add proper indexes for query performance

## Query Examples

### Basic Queries
```python
# Get all serves for a user
user_serves = await Serve.filter(created_by=user).all()

# Get latest artifact for a serve
latest_artifact = await Artifact.filter(serve=serve).order_by('-created_at').first()

# Get active deployment for serve in environment
active_deployment = await ActiveDeployment.filter(
    serve=serve, 
    environment=environment
).prefetch_related('deployment__artifact').first()
```

### Complex Queries with Relationships
```python
# Get all deployments with related data
deployments = await Deployment.filter(serve=serve).prefetch_related(
    'artifact', 'environment', 'created_by'
).all()

# Get serves with their latest artifacts
serves_with_artifacts = await Serve.all().prefetch_related('artifacts_serve')
```

For more examples, see the service layer implementations in `core/src/ml_serve_core/service/`.

## Testing

Model tests should cover:
- Model creation and validation
- Foreign key relationships
- Unique constraints
- Enum validations
- JSON field operations

Run model tests:
```bash
pytest model/tests/
```