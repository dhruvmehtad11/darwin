#!/bin/bash
set -e

# Configuration
RELEASE_NAME="${HELM_RELEASE_NAME:-darwin}"
NAMESPACE="${NAMESPACE:-darwin}"
LOCAL_K8S="${LOCAL_K8S:-true}"
MYSQL_HOST_PATH="${MYSQL_HOST_PATH:-/mnt/shared-data/mysql-0}"
MYSQL_PVC_NAME="${RELEASE_NAME}-mysql-pvc"
MYSQL_DEPLOYMENT_NAME="${RELEASE_NAME}-mysql"

echo "🧹 Clearing MySQL volume for release: $RELEASE_NAME"
echo "   Namespace: $NAMESPACE"
echo "   Local K8s: $LOCAL_K8S"

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl is not installed or not in PATH"
    exit 1
fi

# Check if namespace exists
if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
    echo "ℹ️  Namespace '$NAMESPACE' does not exist. Nothing to clean."
    exit 0
fi

# Check if MySQL deployment exists
if ! kubectl get deployment "$MYSQL_DEPLOYMENT_NAME" -n "$NAMESPACE" &> /dev/null; then
    echo "ℹ️  MySQL deployment '$MYSQL_DEPLOYMENT_NAME' does not exist in namespace '$NAMESPACE'."
else
    echo "📉 Scaling down MySQL deployment..."
    kubectl scale deployment "$MYSQL_DEPLOYMENT_NAME" -n "$NAMESPACE" --replicas=0
    
    echo "⏳ Waiting for MySQL pods to terminate..."
    kubectl wait --for=delete pod \
        -l app.kubernetes.io/component=mysql,app.kubernetes.io/instance="$RELEASE_NAME" \
        -n "$NAMESPACE" \
        --timeout=60s || true
fi

if [ "$LOCAL_K8S" = "true" ]; then
    echo "🗂️  Clearing local hostPath volume"
    
    # For Kind clusters, the path inside the cluster is /mnt/shared-data/mysql-0
    # which maps to ./kind/shared-storage/mysql-0 on the host
    CLUSTER_PATH="/mnt/shared-data/mysql-0"
    
    # Try to clear using a Kubernetes pod (works for Kind and other local setups)
    echo "   Using Kubernetes pod to clear volume..."
    
    # Create cleanup pod to reliably clear the volume
    cleanup_pod_name="mysql-volume-cleanup-$(date +%s)"
    
    # Check if cleanup pod already exists and delete it
    kubectl delete pod "$cleanup_pod_name" -n "$NAMESPACE" 2>/dev/null || true
    
    # Create a temporary pod to clear the volume
    if kubectl run "$cleanup_pod_name" \
        --image=busybox:1.36 \
        --restart=Never \
        --namespace="$NAMESPACE" \
        --overrides='
        {
          "spec": {
            "containers": [{
              "name": "cleanup",
              "image": "busybox:1.36",
              "command": ["sh", "-c", "rm -rf /mnt/mysql-0/* /mnt/mysql-0/.[^.]* 2>/dev/null; exit 0"],
              "volumeMounts": [{
                "name": "mysql-data",
                "mountPath": "/mnt/mysql-0"
              }]
            }],
            "volumes": [{
              "name": "mysql-data",
              "hostPath": {
                "path": "/mnt/shared-data/mysql-0",
                "type": "DirectoryOrCreate"
              }
            }]
          }
        }' 2>&1; then
        
        # Wait for pod to complete (with restartPolicy=Never, pod will be Succeeded when done)
        echo "   Waiting for cleanup pod to complete..."
        sleep 2  # Give pod a moment to start
        max_wait=30
        elapsed=0
        while [ $elapsed -lt $max_wait ]; do
            pod_phase=$(kubectl get pod "$cleanup_pod_name" -n "$NAMESPACE" -o jsonpath='{.status.phase}' 2>/dev/null || echo "NotFound")
            if [ "$pod_phase" = "Succeeded" ] || [ "$pod_phase" = "Failed" ]; then
                break
            fi
            sleep 1
            elapsed=$((elapsed + 1))
        done
        
        pod_phase=$(kubectl get pod "$cleanup_pod_name" -n "$NAMESPACE" -o jsonpath='{.status.phase}' 2>/dev/null || echo "NotFound")
        if [ "$pod_phase" = "Succeeded" ]; then
            echo "✅ Volume cleared successfully via pod"
        elif [ "$pod_phase" = "Failed" ]; then
            echo "⚠️  Pod failed. Trying fallback method..."
        else
            echo "⚠️  Pod did not complete in time. Trying fallback method..."
        fi
        
        kubectl delete pod "$cleanup_pod_name" -n "$NAMESPACE" 2>/dev/null || true
        
        # If pod succeeded, we're done. Otherwise try fallback
        if [ "$pod_phase" != "Succeeded" ]; then
            
            # Try to find Kind shared storage path (relative to script location)
            SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
            KIND_STORAGE_PATH="$SCRIPT_DIR/kind/shared-storage/mysql-0"
            
            if [ -d "$KIND_STORAGE_PATH" ]; then
                echo "   Found Kind storage path: $KIND_STORAGE_PATH"
                rm -rf "${KIND_STORAGE_PATH:?}"/*
                rm -rf "${KIND_STORAGE_PATH:?}"/.[^.]* 2>/dev/null || true
                echo "✅ Volume cleared from Kind host path"
            else
                echo "⚠️  Cannot find Kind storage path. You may need to clear manually:"
                echo "   rm -rf $KIND_STORAGE_PATH/*"
                echo ""
                echo "   Or the volume path inside the cluster: $CLUSTER_PATH"
            fi
        fi
    else
        echo "⚠️  Failed to create cleanup pod. Trying direct host path access..."
        
        # Try to find Kind shared storage path (relative to script location)
        SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
        KIND_STORAGE_PATH="$SCRIPT_DIR/kind/shared-storage/mysql-0"
        
        if [ -d "$KIND_STORAGE_PATH" ]; then
            echo "   Found Kind storage path: $KIND_STORAGE_PATH"
            rm -rf "${KIND_STORAGE_PATH:?}"/*
            rm -rf "${KIND_STORAGE_PATH:?}"/.[^.]* 2>/dev/null || true
            echo "✅ Volume cleared from Kind host path"
        else
            echo "⚠️  Cannot find Kind storage path. You may need to clear manually:"
            echo "   rm -rf $KIND_STORAGE_PATH/*"
            echo ""
            echo "   Or the volume path inside the cluster: $CLUSTER_PATH"
        fi
    fi
else
    echo "🗑️  Deleting PVC: $MYSQL_PVC_NAME"
    
    if kubectl get pvc "$MYSQL_PVC_NAME" -n "$NAMESPACE" &> /dev/null; then
        # Delete the PVC
        kubectl delete pvc "$MYSQL_PVC_NAME" -n "$NAMESPACE" --wait=true --timeout=60s
        echo "✅ PVC deleted successfully"
    else
        echo "ℹ️  PVC '$MYSQL_PVC_NAME' does not exist"
    fi
    
    # Also try to find and delete associated PV if it exists (Retain policy)
    echo "🔍 Checking for associated PersistentVolume..."
    pv_name=$(kubectl get pvc "$MYSQL_PVC_NAME" -n "$NAMESPACE" -o jsonpath='{.spec.volumeName}' 2>/dev/null || echo "")
    if [ -n "$pv_name" ] && kubectl get pv "$pv_name" &> /dev/null; then
        echo "   Found PV: $pv_name"
        # Only delete if it's in Released state (after PVC deletion)
        pv_phase=$(kubectl get pv "$pv_name" -o jsonpath='{.status.phase}' 2>/dev/null || echo "")
        if [ "$pv_phase" = "Released" ]; then
            echo "   Deleting released PV..."
            kubectl delete pv "$pv_name"
            echo "✅ PV deleted"
        fi
    fi
fi

echo ""
echo "✅ MySQL volume cleanup completed!"
echo ""
echo "💡 To restart MySQL with fresh volume, run:"
echo "   helm upgrade --install $RELEASE_NAME ./helm/darwin --namespace $NAMESPACE"

