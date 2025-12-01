# Darwin ML Serve

A scalable machine learning model serving platform

## Overview

Darwin ML Serve provides a comprehensive solution for deploying, managing, and scaling machine learning models in production environments. It acts as a control plane for ML model deployments, handling lifecycle management, versioning, and auto-scaling across multiple environments.

This repository manages:
- **Serve lifecycle** - Create, deploy, update, and undeploy ML services
- **Artifact management** - Build and version deployment artifacts
- **Environment management** - Multi-environment deployment support with per-environment configuration
- **Auto-scaling** - Kubernetes HPA-based horizontal pod autoscaling
- **Infrastructure abstraction** - Works with Darwin Cluster Manager (DCM) for K8s operations

## Repository Structure

```
.
├── README.md                   # Project documentation
├── env.example                 # Environment variables template
├── app_layer/                  # FastAPI REST API layer
│   ├── src/
│   │   └── ml_serve_app_layer/ # API endpoints and request handling
│   └── tests/
├── core/                       # Core business logic
│   ├── src/
│   │   └── ml_serve_core/      
│   │       ├── client/         # External service clients (DCM, MySQL)
│   │       ├── config/         # Configuration management
│   │       ├── constants/      # Application constants
│   │       ├── resources/      # YAML templates (FastAPI, Ray)
│   │       ├── service/        # Business logic services
│   │       └── utils/          # Utility functions
│   └── tests/
├── model/                      # Database models
│   ├── src/
│   │   └── ml_serve_model/     # Tortoise ORM models
│   └── tests/
└── resources/
    └── db/
        └── mysql/
            └── migrations/     # Database migration scripts
```

## Features

- 🚀 **FastAPI & Ray Serve** - Deploy models using FastAPI or Ray Serve backends
- 🔄 **Lifecycle Management** - Complete deployment lifecycle with versioning and rollback
- ⚖️ **Auto-scaling** - Kubernetes HPA with scheduled scaling strategies
- 🌍 **Multi-Environment** - Deploy across multiple environments (local, prod, custom)
- 📦 **Artifact Management** - Build and manage deployment artifacts
- 🔧 **Flexible Configuration** - Environment-based configuration with sensible defaults

## Prerequisites

- Python 3.9+
- Kubernetes cluster (minikube/kind for local, or cloud)
- MySQL 8.0+
- Darwin Cluster Manager (DCM) - [open source coming soon]
- Container registry (Docker Hub, ECR, GCR, etc.)

## Environment Variables

| Variable                      | Description                                    | Default                                            |
|-------------------------------|------------------------------------------------|----------------------------------------------------|
| **Core Configuration**        |                                                |                                                    |
| `ENV`                         | Environment name (local/prod)                  | `local`                                            |
| **Database Configuration**    |                                                |                                                    |
| `MYSQL_HOST`                  | MySQL master host                              | `localhost`                                        |
| `MYSQL_SLAVE_HOST`            | MySQL slave host (optional)                    | Falls back to `MYSQL_HOST`                         |
| `MYSQL_DATABASE`              | Database name                                  | `darwin_ml_serve`                                  |
| `MYSQL_USERNAME`              | Database user                                  | `root`                                             |
| `MYSQL_PASSWORD`              | Database password                              | `password`                                         |
| **Service URLs**              |                                                |                                                    |
| `DCM_URL`                     | Darwin Cluster Manager internal URL            | `http://localhost:8080`                            |
| `ARTIFACT_BUILDER_URL`        | Artifact Builder internal service URL          | `http://localhost:8081`                            |
| `ARTIFACT_BUILDER_PUBLIC_URL` | Artifact Builder public/external URL           | `http://localhost/artifact-builder`                |
| **GitHub Configuration**      |                                                |                                                    |
| `GITHUB_TOKEN`                | GitHub personal access token (for private repos) | ``                                               |
| **Optional**                  |                                                |                                                    |
| `ENABLE_OTEL`                 | Enable OpenTelemetry exporters/instrumentation | Auto (disabled for `ENV=local`, enabled otherwise) |
| `OTEL_SERVICE_NAME`           | Service name for telemetry                     | `darwin-ml-serve`                                  |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | OTLP collector endpoint                        | `http://localhost:4318`                            |
| **Kubernetes Configuration**  |                                                |                                                    |
| `KUBE_INGRESS_CLASS`          | Kubernetes ingress class                       | `alb`                                              |
| **Container Registry**        |                                                |                                                    |
| `CONTAINER_REGISTRY`          | Container registry URL                         | `docker.io`                                        |
| `IMAGE_REPOSITORY`            | Image repository path                          | `darwin`                                           |
| `IMAGE_TAG`                   | Default image tag                              | `latest`                                           |
| `DEFAULT_RUNTIME`             | Default runtime image for one-click deployments | `docker.io/python:3.9-slim`                       |
| **MLflow Configuration**      |                                                |                                                    |
| `MLFLOW_TRACKING_URI`         | MLflow tracking server URL                     | ``                                                 |
| `MLFLOW_TRACKING_USERNAME`    | MLflow username (passed to deployed models)    | ``                                                 |
| `MLFLOW_TRACKING_PASSWORD`    | MLflow password (passed to deployed models)    | ``                                                 |
| **Deployed Service Configuration** |                                           |                                                    |
| `APPLICATION_PORT`            | Port for deployed FastAPI services (not control plane) | `8000`                                      |
| `HEALTHCHECK_PATH`            | Health check path for deployed services        | `/healthcheck`                                     |
| `ORGANIZATION_NAME`           | Organization name for tagging                  | `my-org`                                           |
| **Service Mesh (Optional)**   |                                                |                                                    |
| `ENABLE_ISTIO`                | Enable Istio service mesh                      | `false`                                            |
| `ISTIO_SERVICE_NAME`          | Istio service name                             | `istio-ingressgateway`                             |
| `ISTIO_NAMESPACE`             | Istio namespace                                | `istio-system`                                     |
| **ALB Configuration (Optional)** |                                             |                                                    |
| `ALB_LOGS_ENABLED`            | Enable ALB access logs                         | `false`                                            |
| `ALB_LOGS_BUCKET`             | S3 bucket for ALB logs                         | ``                                                 |
| `ALB_LOGS_PREFIX`             | Prefix for ALB logs in S3                      | `alb-logs`                                         |
| **Workflow Serves (Optional)** |                                               |                                                    |
| `JOB_CLUSTER_RUNTIME`         | Runtime for workflow job clusters              | ``                                                 |

See [env.example](env.example) for complete list of environment variables.

## Project Setup Instructions

### PyCharm Setup

1. **Mark source directories**: Right-click on each `src` directory → Mark Directory as → Sources Root
   - `app_layer/src`
   - `core/src`
   - `model/src`

2. **Mark test directories**: Right-click on each `tests` directory → Mark Directory as → Test Sources Root

3. **Install dependencies**:
   ```bash
   # From workspace root - install all packages in editable mode
   pip install -r app_layer/requirements.txt
   
   # Or install individually (from workspace root)
   pip install -e model/
   pip install -e core/
   pip install -e app_layer/
   
   # Development dependencies (linting, testing)
   pip install -r core/requirements_dev.txt
   ```

4. **Configure environment**:
   ```bash
   cp env.example .env
   # Edit .env with your local configuration
   ```

5. **Setup database**:
   ```bash
   # Using Docker
   docker run -d \
     --name darwin-mysql \
     -e MYSQL_ROOT_PASSWORD=password \
     -e MYSQL_DATABASE=darwin_ml_serve \
     -p 3306:3306 \
     mysql:8.0
   ```

   > **MySQL authentication note:** The default MySQL 8.0 configuration ships with `caching_sha2_password`. When TLS is disabled (the local Docker image above), the driver performs an RSA exchange that depends on the `cryptography` package—this project ships it by default so local installs “just work.” In production, prefer enabling TLS for the database connection; once TLS is in place (or if you switch the user to `mysql_native_password`) you may remove the dependency if desired.

6. **Run the application**:
   - Create a FastAPI run configuration:
     - **Application file**: `app_layer/src/ml_serve_app_layer/main.py`
     - **Working directory**: `app_layer`
     - **Environment variables**: Load from `.env` file
   - Or run from terminal:
     ```bash
     cd app_layer/src
     uvicorn ml_serve_app_layer.main:app --reload --host 0.0.0.0 --port 8007
     ```

## API Documentation

Once the service is running, access:
- **Swagger UI**: http://localhost:8007/docs
- **ReDoc**: http://localhost:8007/redoc
- **Health Check**: http://localhost:8007/healthcheck

## Environment Management

Environments are now fully managed via the API, allowing you to create and configure multiple deployment targets without code changes or application restarts.

### Creating an Environment

Use the `/api/v1/environment` endpoint to create new environments:

```bash
POST /api/v1/environment
{
  "name": "prod",
  "environment_configs": {
    "domain_suffix": ".mycompany.com",
    "cluster_name": "prod-k8s-cluster",
    "namespace": "ml-serve",
    "security_group": "sg-12345, sg-67890",
    "subnets": "subnet-abc123, subnet-def456",
    "ft_redis_url": "redis://prod-redis:6379",
    "workflow_url": "http://workflow-service:8080"
  }
}
```

### Environment Configuration Fields

| Field | Description | Required |
|-------|-------------|----------|
| `domain_suffix` | Domain suffix for service URLs (e.g., `.mycompany.com`) | Yes |
| `cluster_name` | Kubernetes cluster name | Yes |
| `namespace` | Kubernetes namespace for deployments | Yes |
| `security_group` | AWS security groups (comma-separated) | Yes (can be empty) |
| `subnets` | AWS subnets for ALB (comma-separated) | No (defaults to empty) |
| `ft_redis_url` | Redis URL for feature store | Yes (can be empty) |
| `workflow_url` | Workflow service URL | Yes (can be empty) |

### Managing Environments

```bash
# List all environments
GET /api/v1/environment

# Get specific environment
GET /api/v1/environment/{environment_name}

# Update environment configuration
PATCH /api/v1/environment/{environment_name}
{
  "environment_configs": {
    "domain_suffix": ".updated-domain.com",
    ...
  }
}

# Delete environment
DELETE /api/v1/environment/{environment_name}
```

## Key API Workflows

### One-Click Model Deployment

Deploy MLflow models directly without creating serves or artifacts:

```bash
POST /api/v1/serve/deploy-model
{
  "serve_name": "my-model",
  "model_uri": "mlflow-artifacts:/45/abc123.../artifacts/model",
  "env": "prod",
  "cores": 4,
  "memory": 8,
  "node_capacity": "spot",
  "min_replicas": 1,
  "max_replicas": 3
}
```

**Note:** The `env` field must reference an existing environment created via the environment API.

### One-Click Model Undeploy

Stop and remove a model that was deployed via the one-click deployment API:

```bash
POST /api/v1/serve/undeploy-model
{
  "serve_name": "my-model",
  "env": "prod"
}
```

This stops the Kubernetes deployment and removes the running pods for the specified model serve.

### Standard Deployment Workflow

For more control over the deployment process:

1. **Create a Serve**
   ```bash
   POST /api/v1/serve
   {
     "name": "my-model-serve",
     "type": "api",
     "description": "Production model serving",
     "space": "ml-team"
   }
   ```

2. **Configure Infrastructure per Environment**
   ```bash
   POST /api/v1/serve/{serve_name}/infra-config/{env}
   {
     "api_serve_infra_config": {
       "backend_type": "fastapi",
       "cores": 4,
       "memory": 8,
       "min_replicas": 2,
       "max_replicas": 10,
       "node_capacity_type": "spot"
     }
   }
   ```

3. **Create an Artifact**
   ```bash
   POST /api/v1/artifact
   {
     "serve_name": "my-model-serve",
     "version": "v1.0.0",
     "github_repo_url": "https://github.com/myorg/my-model",
     "branch": "main"
   }
   ```

4. **Deploy to Environment**
   ```bash
   POST /api/v1/serve/{serve_name}/deploy
   {
     "env": "prod",
     "artifact_version": "v1.0.0",
     "api_serve_deployment_config": {
       "deployment_strategy": "rolling",
       "environment_variables": {
         "MODEL_PATH": "/models/production"
       }
     }
   }
   ```

### Updating Infrastructure Configuration

Update resource limits, replicas, or other infra settings for a deployed serve:

```bash
PATCH /api/v1/serve/{serve_name}/infra-config/{env}
{
  "api_serve_infra_config": {
    "min_replicas": 5,
    "max_replicas": 20,
    "cores": 8,
    "memory": 16
  }
}
```

**Automatic Redeployment:** If a serve is already deployed, updating the infra config will automatically trigger a redeployment with the new settings.

## Development Workflow

### Running Tests

```bash
# Run all tests
pytest

# Run specific module tests
pytest app_layer/tests/
pytest core/tests/
pytest model/tests/

# Run with coverage
pytest --cov=ml_serve_app_layer --cov=ml_serve_core --cov=ml_serve_model
```

### Code Formatting

```bash
# Format code with black
black app_layer/ core/ model/ -l 120

# Check with flake8
flake8 app_layer/ core/ model/
```

### Database Management

This project uses **Tortoise ORM** for database management with automatic schema generation. The database schema is defined using Python models in the `model` package.

**Key Features:**
- **Automatic Schema Generation**: Tables are created automatically from model definitions
- **No Manual Migrations**: Schema changes are handled by updating model classes
- **Model-First Approach**: Database structure follows Python model definitions

**Database Configuration:**
- Configuration files: `resources/config/mysql/connection-*.conf`
- Models location: `model/src/ml_serve_model/`
- Automatic initialization on application startup

For detailed information about database models, table structures, and relationships, see the [Model Package README](model/README.md).


## Deployment

### Local Development with Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Kubernetes Deployment

```bash
# Create namespace
kubectl create namespace darwin-ml-serve

# Apply manifests
kubectl apply -f k8s/

# Verify deployment
kubectl get pods -n darwin-ml-serve
kubectl get svc -n darwin-ml-serve

# Check logs
kubectl logs -f deployment/darwin-ml-serve -n darwin-ml-serve
```

## Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feat/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feat/amazing-feature`)
5. Open a Pull Request

Please ensure:
- Code follows existing style (Black formatter with 120 line length)
- Tests pass (`pytest`)
- Documentation is updated

## License

[License to be determined - MIT/Apache 2.0 recommended]

## Acknowledgments

Darwin ML Serve was originally developed internally and is now open-sourced to benefit the machine learning community.
