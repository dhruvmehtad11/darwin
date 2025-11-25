# Adding a New Service

Step-by-step guide to add a new microservice to the Darwin platform.

---

## Overview

Adding a service requires:
1. Create/add the service submodule
2. Add `.odin/` scripts to the submodule
3. Register in `services.yaml`
4. Add Helm deployment config
5. Configure ingress routing (optional)
6. Build and deploy

---

## Step 1: Add Service Submodule

### Option A: New Repository
```bash
# Create directory
mkdir my-service
cd my-service

# Initialize your project
# (Python example)
python -m venv venv
pip install fastapi uvicorn
```

### Option B: Existing Repository (as submodule)
```bash
git submodule add https://github.com/org/my-service.git my-service
```

---

## Step 2: Create .odin Directory

Create `.odin/{service-name}/` with required scripts:

```
my-service/
├── .odin/
│   └── my-service/
│       ├── build.sh      # Required: Compiles the app
│       ├── setup.sh      # Required: Container setup
│       ├── start.sh      # Required: Container entrypoint
│       └── pre-deploy.sh # Optional: DB migrations
├── src/
│   └── main.py
└── requirements.txt
```

### build.sh (Example: Python)
```bash
#!/bin/bash
set -e

SERVICE_NAME="my-service"
mkdir -p target/$SERVICE_NAME

# Copy source code
cp -r src/* target/$SERVICE_NAME/
cp requirements.txt target/$SERVICE_NAME/

echo "Build completed for $SERVICE_NAME"
```

### build.sh (Example: Java)
```bash
#!/bin/bash
set -e

SERVICE_NAME="my-service"
mvn clean package -DskipTests

mkdir -p target/$SERVICE_NAME
cp target/*.jar target/$SERVICE_NAME/
cp -r config/* target/$SERVICE_NAME/

echo "Build completed for $SERVICE_NAME"
```

### build.sh (Example: Go)
```bash
#!/bin/bash
set -e

SERVICE_NAME="my-service"
go build -o target/$SERVICE_NAME/app ./cmd/main.go

echo "Build completed for $SERVICE_NAME"
```

### setup.sh
```bash
#!/bin/bash
set -e

cd /app

# Install dependencies (Python)
pip install --no-cache-dir -r requirements.txt

# Or for Java: nothing needed, JAR is self-contained
# Or for Go: nothing needed, binary is self-contained

echo "Setup completed"
```

### start.sh
```bash
#!/bin/bash
set -e

cd /app

# Start the service
exec python -m uvicorn main:app --host 0.0.0.0 --port 8000

# Or for Java:
# exec java -jar app.jar

# Or for Go:
# exec ./app
```

### pre-deploy.sh (Optional)
```bash
#!/bin/bash
set -e

cd /app

# Run database migrations
python manage.py migrate

# Or create tables
python scripts/init_db.py
```

---

## Step 3: Register in services.yaml

**File**: `services.yaml`

Add your service to the `applications` list:

```yaml
applications:
  # ... existing services ...

  - application: my-service
    base-path: my-service          # Directory containing .odin/
    path: .                         # Build context (usually ".")
    base-image: darwin/python:3.9.7-pip-bookworm-slim  # Or java/golang
    enabled: true                   # Set true to build
    env:                            # Build-time environment variables
      - name: MY_CONFIG_VAR
        value: "some-value"
      - name: DATABASE_URL
        value: "mysql://darwin-mysql:3306/darwin"
```

**Base image options**:
- Python: `darwin/python:3.9.7-pip-bookworm-slim`
- Java: `darwin/java:11-maven-bookworm-slim`
- Go: `darwin/golang:1.18-bookworm-slim`

---

## Step 4: Add Helm Deployment Config

**File**: `helm/darwin/charts/services/values.yaml`

Add under the `services:` section:

```yaml
services:
  # ... existing services ...

  my-service:
    enabled: true
    replicaCount: 1
    serviceName: my-service
    image:
      registry: localhost:5000
      name: my-service
      tag: latest
      pullPolicy: Always
    service:
      type: ClusterIP
      port: 8000                    # Your service's port
    ingress:
      enabled: false                # Set true for external access
    healthcheck:
      path: /health                 # Health endpoint
      initialDelaySeconds: 30
      readinessDelay: 15
    autoscaling:
      enabled: false
    resources:
      limits:
        cpu: 500m
        memory: 512Mi
      requests:
        cpu: 100m
        memory: 256Mi
    extraEnvVars:
      - name: ENV
        value: "darwin-local"
      - name: LOG_LEVEL
        value: "INFO"
```

---

## Step 5: Add Ingress Routing (Optional)

To expose the service externally via path-based routing:

**File**: `helm/darwin/charts/services/values.yaml`

Add to the `ingress.paths` section:

```yaml
ingress:
  enabled: true
  className: "nginx"
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /$2
    nginx.ingress.kubernetes.io/use-regex: "true"
  host: "localhost"
  paths:
    # ... existing paths ...
    - path: "/my-service(/|$)(.*)"
      service: "my-service"
      port: 8000
```

This routes `localhost/my-service/*` to your service.

---

## Step 6: Build and Deploy

### Build the Image
```bash
sh setup.sh -y
```

Or build just your service:
```bash
source config.env
sh deployer/scripts/image-builder.sh \
  -a "my-service" \
  -t "my-service" \
  -p "." \
  -e "darwin/python:3.9.7-pip-bookworm-slim" \
  -r "$DOCKER_REGISTRY"
```

### Deploy
```bash
sh start.sh
```

Or upgrade existing deployment:
```bash
source config.env
helm upgrade darwin ./helm/darwin -n darwin
```

---

## Step 7: Verify Deployment

### Check Pod Status
```bash
kubectl get pods -n darwin | grep my-service
```

### Check Logs
```bash
kubectl logs -n darwin deployment/darwin-my-service -f
```

### Test Endpoint
```bash
# If ingress enabled:
curl http://localhost/my-service/health

# Port-forward for testing:
kubectl port-forward -n darwin svc/darwin-my-service 8000:8000
curl http://localhost:8000/health
```

---

## Complete Example: Python FastAPI Service

### Directory Structure
```
my-service/
├── .odin/
│   └── my-service/
│       ├── build.sh
│       ├── setup.sh
│       └── start.sh
├── src/
│   └── main.py
└── requirements.txt
```

### src/main.py
```python
from fastapi import FastAPI

app = FastAPI()

@app.get("/health")
def health():
    return {"status": "healthy"}

@app.get("/")
def root():
    return {"message": "Hello from my-service"}
```

### requirements.txt
```
fastapi==0.104.1
uvicorn==0.24.0
```

### .odin/my-service/build.sh
```bash
#!/bin/bash
set -e
mkdir -p target/my-service
cp -r src/* target/my-service/
cp requirements.txt target/my-service/
```

### .odin/my-service/setup.sh
```bash
#!/bin/bash
set -e
cd /app
pip install --no-cache-dir -r requirements.txt
```

### .odin/my-service/start.sh
```bash
#!/bin/bash
set -e
cd /app
exec python -m uvicorn main:app --host 0.0.0.0 --port 8000
```

---

## Checklist

- [ ] Service directory exists with source code
- [ ] `.odin/{service}/build.sh` creates `target/{service}/`
- [ ] `.odin/{service}/setup.sh` installs runtime deps
- [ ] `.odin/{service}/start.sh` starts the service
- [ ] `services.yaml` has entry with `enabled: true`
- [ ] `helm/darwin/charts/services/values.yaml` has deployment config
- [ ] (Optional) Ingress path configured
- [ ] Image builds successfully: `docker images | grep my-service`
- [ ] Pod runs: `kubectl get pods -n darwin | grep my-service`

---

## Related Prompts

- **Build system details**: `.prompts/01-build-system.md`
- **Helm configuration**: `.prompts/05-helm-charts.md`
- **Service configuration**: `.prompts/07-services.md`

