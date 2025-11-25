# Troubleshooting Guide

Common issues and how to resolve them.

---

## Quick Diagnostics

### Check Overall Status
```bash
# All pods
kubectl get pods -A

# Darwin namespace
kubectl get pods -n darwin

# Events (recent issues)
kubectl get events -n darwin --sort-by='.lastTimestamp'
```

### Check Specific Component
```bash
# Pod details
kubectl describe pod <pod-name> -n darwin

# Logs
kubectl logs <pod-name> -n darwin

# Previous container logs (after crash)
kubectl logs <pod-name> -n darwin --previous
```

---

## Setup Issues

### Kind Cluster Won't Start

**Symptom**: `kind create cluster` fails

**Solutions**:
```bash
# 1. Check Docker is running
docker info

# 2. Check port conflicts
lsof -i :80
lsof -i :443
lsof -i :6443

# 3. Remove existing cluster and retry
kind delete cluster --name kind
sh kind/start-cluster.sh

# 4. Check disk space
df -h
```

### Can't Connect to Cluster

**Symptom**: `kubectl` commands fail with connection refused

**Solutions**:
```bash
# 1. Set correct KUBECONFIG
export KUBECONFIG=./kind/config/kindkubeconfig.yaml

# 2. Check cluster is running
docker ps | grep kind

# 3. Verify cluster info
kubectl cluster-info --context kind-kind

# 4. If cluster container stopped
docker start kind-control-plane kind-worker kind-worker2
```

### Registry Not Accessible

**Symptom**: Image push fails

**Solutions**:
```bash
# 1. Check registry container
docker ps | grep kind-registry

# 2. Restart registry
docker stop kind-registry && docker rm kind-registry
docker run -d --restart=always -p 0:5000 \
  --network kind --name kind-registry registry:2

# 3. Update config.env
REGISTRY_PORT=$(docker port kind-registry 5000/tcp | cut -d: -f2)
echo "DOCKER_REGISTRY=127.0.0.1:$REGISTRY_PORT" > config.env
echo "KUBECONFIG=./kind/config/kindkubeconfig.yaml" >> config.env

# 4. Verify
curl http://127.0.0.1:$REGISTRY_PORT/v2/_catalog
```

---

## Build Issues

### Build Script Fails

**Symptom**: `setup.sh` fails during image build

**Solutions**:
```bash
# 1. Check submodules are initialized
git submodule update --init --recursive

# 2. Verify .odin directory exists
ls -la <service-dir>/.odin/<service-name>/

# 3. Run build manually for debugging
cd <service-dir>
bash .odin/<service-name>/build.sh

# 4. Check target directory
ls -la <service-dir>/target/<service-name>/
```

### Docker Build Fails

**Symptom**: Docker build error

**Solutions**:
```bash
# 1. Check base image exists
docker images | grep darwin/

# 2. Rebuild base images
cd deployer/images/python-3.9.7 && sh build.sh
cd deployer/images/java-11 && sh build.sh
cd deployer/images/golang-1.18 && sh build.sh

# 3. Check Dockerfile syntax
docker build --no-cache -f deployer/images/Dockerfile \
  --build-arg BASE_IMAGE=darwin/python:3.9.7-pip-bookworm-slim \
  --build-arg APP_NAME=<service> \
  --build-arg APP_BASE_DIR=<base-path> \
  --build-arg APP_DIR=. .
```

### Image Push Fails

**Symptom**: `docker push` fails

**Solutions**:
```bash
# 1. Verify registry
source config.env
curl http://$DOCKER_REGISTRY/v2/_catalog

# 2. Check image is tagged correctly
docker images | grep <service-name>

# 3. Re-tag and push
docker tag <service-name>:latest $DOCKER_REGISTRY/<service-name>:latest
docker push $DOCKER_REGISTRY/<service-name>:latest
```

---

## Deployment Issues

### Helm Install Fails

**Symptom**: `helm upgrade --install` fails

**Solutions**:
```bash
# 1. Check helm syntax
helm lint ./helm/darwin

# 2. Dry run to see rendered templates
helm upgrade --install darwin ./helm/darwin \
  --namespace darwin --dry-run --debug

# 3. Update dependencies
cd helm/darwin && helm dependency update

# 4. Check for existing resources
kubectl get all -n darwin
```

### Pods Stuck in Pending

**Symptom**: Pods won't start, stuck in `Pending`

**Solutions**:
```bash
# 1. Check events
kubectl describe pod <pod-name> -n darwin

# 2. Check node resources
kubectl describe nodes | grep -A 5 "Allocated resources"

# 3. Check PVC if using persistence
kubectl get pvc -n darwin

# 4. Check if image exists
kubectl get pod <pod-name> -n darwin -o yaml | grep image
docker images | grep <image-name>
```

### Pods Stuck in ImagePullBackOff

**Symptom**: Can't pull image from registry

**Solutions**:
```bash
# 1. Check image exists in registry
source config.env
curl http://$DOCKER_REGISTRY/v2/_catalog
curl http://$DOCKER_REGISTRY/v2/<image-name>/tags/list

# 2. Check image pull policy
kubectl get pod <pod-name> -n darwin -o yaml | grep pullPolicy

# 3. Verify registry is accessible from cluster
kubectl run test --rm -it --image=busybox -- \
  wget -qO- http://kind-registry:5000/v2/_catalog
```

### Pods CrashLoopBackOff

**Symptom**: Container keeps restarting

**Solutions**:
```bash
# 1. Check logs
kubectl logs <pod-name> -n darwin
kubectl logs <pod-name> -n darwin --previous

# 2. Check start.sh script
kubectl exec -it <pod-name> -n darwin -- cat /app/.odin/start.sh

# 3. Test startup manually
kubectl exec -it <pod-name> -n darwin -- bash
cd /app && bash .odin/start.sh

# 4. Check environment variables
kubectl exec <pod-name> -n darwin -- env | sort
```

---

## Service Issues

### Service Not Responding

**Symptom**: Can't reach service endpoint

**Solutions**:
```bash
# 1. Check pod is running
kubectl get pods -n darwin | grep <service>

# 2. Check service exists
kubectl get svc -n darwin | grep <service>

# 3. Port forward and test
kubectl port-forward -n darwin svc/darwin-<service> 8000:8000
curl http://localhost:8000/health

# 4. Check endpoints
kubectl get endpoints -n darwin darwin-<service>
```

### Health Check Failing

**Symptom**: Pod keeps restarting due to failed health check

**Solutions**:
```bash
# 1. Check health endpoint manually
kubectl exec <pod-name> -n darwin -- curl -s localhost:8000/health

# 2. Increase initialDelaySeconds in values.yaml
healthcheck:
  initialDelaySeconds: 120  # Increase if slow startup

# 3. Check application logs during startup
kubectl logs <pod-name> -n darwin -f
```

### Ingress Not Working

**Symptom**: External requests return 404 or 502

**Solutions**:
```bash
# 1. Check ingress controller
kubectl get pods -n ingress-nginx

# 2. Check ingress resources
kubectl get ingress -n darwin
kubectl describe ingress -n darwin

# 3. Check ingress logs
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller

# 4. Verify service is reachable
kubectl exec -n ingress-nginx deployment/ingress-nginx-controller -- \
  curl -s darwin-<service>.darwin.svc.cluster.local:8000/health
```

---

## Datastore Issues

### MySQL Not Starting

**Symptom**: MySQL pod in CrashLoopBackOff

**Solutions**:
```bash
# 1. Check logs
kubectl logs -n darwin deployment/darwin-mysql

# 2. Check PVC
kubectl get pvc -n darwin | grep mysql

# 3. Delete PVC and redeploy (data loss!)
kubectl delete pvc darwin-mysql-pvc -n darwin
helm upgrade darwin ./helm/darwin -n darwin
```

### Can't Connect to MySQL

**Symptom**: Connection refused to MySQL

**Solutions**:
```bash
# 1. Check MySQL is running
kubectl get pods -n darwin | grep mysql

# 2. Test connection from within cluster
kubectl run mysql-test --rm -it --image=mysql:8 -- \
  mysql -h darwin-mysql -u root -ppassword -e "SELECT 1"

# 3. Check service
kubectl get svc -n darwin | grep mysql
```

### LocalStack Not Working

**Symptom**: S3 operations fail

**Solutions**:
```bash
# 1. Check pod
kubectl get pods -n darwin | grep localstack
kubectl logs -n darwin deployment/darwin-localstack

# 2. Test from within cluster
kubectl exec -n darwin deployment/darwin-localstack -- \
  awslocal s3 ls

# 3. Check endpoint configuration
# Should be: http://darwin-localstack:4566
```

---

## Performance Issues

### Slow Startup

**Symptom**: Services take long time to start

**Solutions**:
```bash
# 1. Check resource limits
kubectl describe pod <pod-name> -n darwin | grep -A 5 "Limits"

# 2. Increase resources in values.yaml
resources:
  requests:
    cpu: 500m
    memory: 1Gi

# 3. Check node capacity
kubectl describe nodes | grep -A 10 "Capacity"
```

### Out of Memory

**Symptom**: Pods OOMKilled

**Solutions**:
```bash
# 1. Check memory usage
kubectl top pods -n darwin

# 2. Increase memory limits
resources:
  limits:
    memory: 2Gi

# 3. Check for memory leaks in application logs
kubectl logs <pod-name> -n darwin | grep -i memory
```

---

## Useful Commands

### Reset Everything
```bash
# Delete Darwin deployment
helm uninstall darwin -n darwin

# Delete namespace
kubectl delete namespace darwin

# Delete Kind cluster
kind delete cluster --name kind

# Start fresh
sh setup.sh -y && sh start.sh
```

### View All Logs
```bash
# All pods in namespace
kubectl logs -n darwin -l darwin-component-type=service --all-containers

# Follow specific deployment
kubectl logs -n darwin deployment/darwin-compute -f
```

### Debug Container
```bash
# Shell into running pod
kubectl exec -it <pod-name> -n darwin -- bash

# Debug pod (if main container crashes)
kubectl debug <pod-name> -n darwin -it --image=busybox
```

### Check Resource Usage
```bash
# Node resources
kubectl top nodes

# Pod resources
kubectl top pods -n darwin

# Detailed pod info
kubectl describe pod <pod-name> -n darwin
```

---

## Getting Help

### Collect Debug Info
```bash
# Export cluster state
kubectl cluster-info dump > cluster-dump.txt

# Export Darwin namespace
kubectl get all -n darwin -o yaml > darwin-state.yaml

# Export events
kubectl get events -n darwin --sort-by='.lastTimestamp' > events.txt
```

---

## Related Prompts

- **Setup**: `.prompts/04-kind-cluster.md`
- **Build**: `.prompts/01-build-system.md`
- **Deployment**: `.prompts/02-deployment.md`
- **Services**: `.prompts/07-services.md`

