# Ingress Controller Setup for Darwin Platform

## Recommended Approach: Helm-Managed ✅

**Helm-managed is recommended** because:
- ✅ **Consistency**: All infrastructure managed in one place (Helm charts)
- ✅ **Version Control**: Configuration tracked in `values.yaml`
- ✅ **Lifecycle Management**: Part of `helm install/upgrade/uninstall`
- ✅ **Customization**: Easy to adjust per environment via values files
- ✅ **Dependency Management**: Already declared in `Chart.yaml` as a dependency
- ✅ **Maintainability**: Single source of truth for all Kubernetes resources
- ✅ **CI/CD Friendly**: Easier to automate deployments

## Comparison

### Manual Installation (Current)
| Pros | Cons |
|------|------|
| ✅ Simple one-command install | ❌ Not managed by Helm lifecycle |
| ✅ Official Kind-optimized YAML | ❌ Can't customize via values.yaml |
| ✅ Guaranteed Kind compatibility | ❌ Manual version management |
|  | ❌ Not part of helm upgrade/uninstall |

### Helm-Managed (Recommended) ✅
| Pros | Cons |
|------|------|
| ✅ Unified management with all resources | ⚠️ Need to ensure Kind config is correct |
| ✅ Version controlled configuration | ⚠️ Slightly more complex values.yaml |
| ✅ Part of deployment lifecycle | |
| ✅ Customizable per environment | |
| ✅ Easier to maintain and update | |

## Verification Steps

To verify your ingress controller is working correctly:

1. **Check if ingress controller pod is running:**
   ```bash
   kubectl get pods -n ingress-nginx
   ```

2. **Verify the service is exposed:**
   ```bash
   kubectl get svc -n ingress-nginx
   ```
   The service should be of type `NodePort` or should use `hostNetwork: true` in the pod spec.

3. **Check ingress controller logs:**
   ```bash
   kubectl logs -n ingress-nginx -l app.kubernetes.io/component=controller --tail=50
   ```

4. **Verify your ingress resources:**
   ```bash
   kubectl get ingress -n darwin
   kubectl describe ingress darwin-ingress -n darwin
   ```

5. **Check if backend services have endpoints:**
   ```bash
   kubectl get svc darwin-mlflow-app -n darwin
   kubectl get endpoints darwin-mlflow-app -n darwin
   kubectl get pods -n darwin -l app.kubernetes.io/component=mlflow-app
   ```

## Troubleshooting

### Empty Reply from Server

If you get "Empty reply from server" when accessing `http://localhost/mlflow-app/health`:

1. **Verify ingress resource exists and is correctly configured:**
   ```bash
   kubectl get ingress darwin-ingress -n darwin -o yaml
   ```

2. **Check if the service name matches:**
   - Ingress backend service: `darwin-mlflow-app`
   - Actual service name should be: `darwin-mlflow-app`
   ```bash
   kubectl get svc -n darwin | grep mlflow-app
   ```

3. **Verify the ingress controller is picking up the ingress:**
   ```bash
   kubectl logs -n ingress-nginx -l app.kubernetes.io/component=controller | grep "darwin-ingress"
   ```

4. **Test backend service directly (bypass ingress):**
   ```bash
   kubectl port-forward svc/darwin-mlflow-app 8000:8000 -n darwin
   # In another terminal:
   curl http://localhost:8000/health
   ```

## Migration to Helm-Managed

To switch from manual to Helm-managed ingress controller:

### Step 1: Remove Manual Installation

Edit `kind/start-cluster.sh` and comment out the manual ingress installation (line 37):

```bash
# Comment out this line:
# kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.0/deploy/static/provider/kind/deploy.yaml
# kubectl label node kind-control-plane ingress-ready=true
```

### Step 2: Enable Helm-Managed Ingress Controller

Update `helm/darwin/values.yaml`:

```yaml
ingress-nginx:
  enabled: true
  
  controller:
    # Tell the subchart to NOT create a new IngressClass resource (reuse existing)
    ingressClassResource:
      name: nginx 
      controllerValue: "k8s.io/ingress-nginx" 
      create: false
      
    ingressClass: nginx
    
    # Configure for Kind clusters - use hostNetwork for port 80/443 exposure
    hostNetwork: true
    dnsPolicy: ClusterFirstWithHostNet
    
    # Service configuration
    service:
      type: NodePort
      nodePorts:
        http: 80
        https: 443
      external:
        enabled: false
    
    # Pod security context
    podSecurityContext:
      runAsNonRoot: true
      runAsUser: 101
      fsGroup: 101
```

### Step 3: Clean Up and Deploy

1. **Remove existing manual ingress controller** (if cluster is already running):
   ```bash
   kubectl delete namespace ingress-nginx
   ```

2. **Update Helm dependencies:**
   ```bash
   cd helm/darwin
   helm dependency update
   ```

3. **Upgrade/Install with Helm:**
   ```bash
   helm upgrade --install darwin ./helm/darwin \
     --namespace darwin \
     --create-namespace \
     --wait \
     --timeout 600s
   ```

## Current Setup

📌 **Current**: Manual installation via `start-cluster.sh`  
✅ **Recommended**: Switch to Helm-managed (see migration steps above)

