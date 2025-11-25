# Kind Cluster Setup

Kind (Kubernetes in Docker) provides a local Kubernetes cluster for development.

---

## Cluster Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Host                               │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐                   │
│  │ kind-control-   │  │ kind-registry   │                   │
│  │ plane           │  │ (localhost:5000)│                   │
│  │ - API Server    │  │                 │                   │
│  │ - Ingress       │  │ Docker Registry │                   │
│  └─────────────────┘  └─────────────────┘                   │
│           │                                                  │
│  ┌────────┴────────┐                                        │
│  │                 │                                        │
│  ▼                 ▼                                        │
│  ┌─────────┐  ┌─────────┐                                   │
│  │ kind-   │  │ kind-   │                                   │
│  │ worker  │  │ worker2 │                                   │
│  └─────────┘  └─────────┘                                   │
│                                                              │
│  Shared Storage: ./kind/shared-storage → /mnt/shared-data   │
└─────────────────────────────────────────────────────────────┘
```

---

## Configuration File

**Location**: `kind/kind-config.yaml`

```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
containerdConfigPatches:
- |-
  [plugins."io.containerd.grpc.v1.cri".registry.mirrors."localhost:5000"]
    endpoint = ["http://kind-registry:5000"]
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 80
    hostPort: 80        # HTTP ingress
    protocol: TCP
  - containerPort: 443
    hostPort: 443       # HTTPS ingress
    protocol: TCP
  - containerPort: 6443
    hostPort: 6443      # Kubernetes API
    listenAddress: "127.0.0.1"
    protocol: TCP
  - containerPort: 30080
    hostPort: 30080     # NodePort range
    protocol: TCP
  extraMounts:
  - hostPath: ./kind/shared-storage
    containerPath: /mnt/shared-data
- role: worker
  extraMounts:
  - hostPath: ./kind/shared-storage
    containerPath: /mnt/shared-data
- role: worker
  extraMounts:
  - hostPath: ./kind/shared-storage
    containerPath: /mnt/shared-data
```

**Key features**:
- 1 control plane + 2 worker nodes
- Ports 80, 443, 6443, 30080 exposed to host
- Shared storage mounted on all nodes
- Local registry mirror configured

---

## Cluster Start Script

**Location**: `kind/start-cluster.sh`

**What it does**:
1. Installs kind if not present
2. Creates cluster if not exists
3. Installs cert-manager
4. Installs ingress-nginx
5. Starts Docker registry (`kind-registry`)

```bash
# Create cluster
kind create cluster \
  --name "${CLUSTER_NAME}" \
  --config "${KIND_CONFIG}" \
  --kubeconfig "${KUBECONFIG}"

# Install cert-manager
helm install cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --set crds.enabled=true

# Install ingress-nginx
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.0/deploy/static/provider/kind/deploy.yaml

# Start local registry
docker run -d --restart=always -p 0:5000 \
  --network kind --name kind-registry registry:2
```

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `CLUSTER_NAME` | `kind` | Cluster name |
| `KIND_CONFIG` | `./kind/kind-config.yaml` | Config file path |
| `KUBECONFIG` | `./kind/config/kindkubeconfig.yaml` | Generated kubeconfig |

---

## Directory Structure

```
kind/
├── kind-config.yaml        # Cluster configuration
├── start-cluster.sh        # Cluster creation script
├── stop-cluster.sh         # Cluster deletion script
├── config/
│   └── kindkubeconfig.yaml # Generated kubeconfig (gitignored)
└── shared-storage/         # Shared volume mounted to all nodes
    └── .m2/                # Maven cache (for Java builds)
```

---

## Commands

### Create Cluster
```bash
# Via setup.sh (recommended)
sh setup.sh

# Or manually
export CLUSTER_NAME=kind
export KIND_CONFIG=./kind/kind-config.yaml
export KUBECONFIG=./kind/config/kindkubeconfig.yaml
sh kind/start-cluster.sh
```

### Check Cluster Status
```bash
# List clusters
kind get clusters

# Get nodes
kubectl get nodes

# Check all pods
kubectl get pods -A
```

### Delete Cluster
```bash
kind delete cluster --name kind
```

### Access Cluster
```bash
# Set kubeconfig
export KUBECONFIG=./kind/config/kindkubeconfig.yaml

# Or use context
kubectl config use-context kind-kind
```

---

## Docker Registry

A local Docker registry runs alongside the cluster:

**Container**: `kind-registry`
**Network**: Connected to `kind` network
**Port**: Dynamic (stored in `config.env` as `DOCKER_REGISTRY`)

### Check Registry
```bash
# Is it running?
docker ps | grep kind-registry

# Get port
docker port kind-registry 5000/tcp

# List images
curl http://127.0.0.1:32768/v2/_catalog
```

### Push Image to Registry
```bash
source config.env
docker tag my-image:latest $DOCKER_REGISTRY/my-image:latest
docker push $DOCKER_REGISTRY/my-image:latest
```

---

## Shared Storage

**Host path**: `./kind/shared-storage/`
**Container path**: `/mnt/shared-data/`

Used for:
- Maven cache (`.m2/`)
- Persistent data between builds
- Shared files across pods

### Access from Pod
```yaml
volumes:
  - name: shared-data
    hostPath:
      path: /mnt/shared-data
      type: Directory
```

---

## Port Mappings

| Host Port | Container Port | Purpose |
|-----------|----------------|---------|
| 80 | 80 | HTTP Ingress |
| 443 | 443 | HTTPS Ingress |
| 6443 | 6443 | Kubernetes API |
| 30080 | 30080 | NodePort services |

### Access Services

After deployment:
```bash
# Via ingress (recommended)
curl http://localhost/feature-store/health

# Via port-forward
kubectl port-forward -n darwin svc/darwin-mysql 3306:3306
```

---

## Installed Components

When cluster is created, these are automatically installed:

### cert-manager
For TLS certificate management:
```bash
kubectl get pods -n cert-manager
```

### ingress-nginx
For HTTP routing:
```bash
kubectl get pods -n ingress-nginx
```

---

## Troubleshooting

### Cluster Won't Start
```bash
# Check Docker is running
docker info

# Check for port conflicts
lsof -i :80
lsof -i :443
lsof -i :6443

# Delete and recreate
kind delete cluster --name kind
sh kind/start-cluster.sh
```

### Can't Connect to Cluster
```bash
# Verify kubeconfig
export KUBECONFIG=./kind/config/kindkubeconfig.yaml
kubectl cluster-info

# Check cluster container is running
docker ps | grep kind
```

### Registry Not Working
```bash
# Restart registry
docker stop kind-registry
docker rm kind-registry
docker run -d --restart=always -p 0:5000 \
  --network kind --name kind-registry registry:2

# Update config.env with new port
REGISTRY_PORT=$(docker port kind-registry 5000/tcp | cut -d: -f2)
echo "DOCKER_REGISTRY=127.0.0.1:$REGISTRY_PORT" >> config.env
```

### Ingress Not Working
```bash
# Check ingress controller
kubectl get pods -n ingress-nginx

# Check ingress resources
kubectl get ingress -A

# Verify control-plane has ingress label
kubectl get node kind-control-plane --show-labels | grep ingress
```

---

## Resource Requirements

Minimum recommended:
- **CPU**: 4 cores
- **Memory**: 8GB RAM
- **Disk**: 20GB free space

### Adjust Node Resources
Edit `kind-config.yaml`:
```yaml
nodes:
- role: control-plane
  kubeadmConfigPatches:
  - |
    kind: InitConfiguration
    nodeRegistration:
      kubeletExtraArgs:
        system-reserved: cpu=500m,memory=500Mi
```

---

## Related Prompts

- **Setup process**: `.prompts/01-build-system.md`
- **Deployment**: `.prompts/02-deployment.md`
- **Troubleshooting**: `.prompts/08-troubleshooting.md`

