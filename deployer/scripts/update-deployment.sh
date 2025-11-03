#!/bin/sh
# Script to update Kubernetes deployment after pushing new image to registry
# Usage: ./update-deployment.sh <deployment-name> [namespace] [method]
# Methods: restart (default), helm-upgrade, annotation

set -e

DEPLOYMENT_NAME=${1}
NAMESPACE=${2:-darwin}
METHOD=${3:-restart}
KUBECONFIG=${KUBECONFIG:-$(pwd)/kind/config/kindkubeconfig.yaml}

if [ -z "$DEPLOYMENT_NAME" ]; then
  echo "❌ Error: Deployment name required"
  echo "Usage: $0 <deployment-name> [namespace] [method]"
  echo ""
  echo "Methods:"
  echo "  restart        - kubectl rollout restart (default, requires pullPolicy: Always)"
  echo "  helm-upgrade   - Helm upgrade with same tag"
  echo "  annotation     - Patch deployment annotation to force rollout"
  exit 1
fi

export KUBECONFIG

case "$METHOD" in
  restart)
    echo "🔄 Method: Rolling out deployment restart for $DEPLOYMENT_NAME..."
    kubectl rollout restart deployment/$DEPLOYMENT_NAME -n $NAMESPACE
    echo "⏳ Waiting for rollout to complete..."
    kubectl rollout status deployment/$DEPLOYMENT_NAME -n $NAMESPACE --timeout=300s
    ;;
    
  helm-upgrade)
    echo "🔧 Method: Helm upgrade for $DEPLOYMENT_NAME..."
    if [ ! -f "helm/darwin/Chart.yaml" ]; then
      echo "❌ Error: helm/darwin/Chart.yaml not found. Run from project root."
      exit 1
    fi
    # Extract service name from deployment (remove darwin- prefix if present)
    SERVICE_NAME=$(echo $DEPLOYMENT_NAME | sed 's/^darwin-//')
    helm upgrade darwin ./helm/darwin \
      --namespace $NAMESPACE \
      --reuse-values \
      --set services.$SERVICE_NAME.image.tag=latest \
      --wait \
      --timeout=300s
    ;;
    
  annotation)
    echo "🏷️  Method: Updating deployment annotation for $DEPLOYMENT_NAME..."
    kubectl patch deployment $DEPLOYMENT_NAME -n $NAMESPACE \
      -p "{\"spec\":{\"template\":{\"metadata\":{\"annotations\":{\"kubectl.kubernetes.io/restartedAt\":\"$(date +%s)\"}}}}}"
    echo "⏳ Waiting for rollout to complete..."
    kubectl rollout status deployment/$DEPLOYMENT_NAME -n $NAMESPACE --timeout=300s
    ;;
    
  *)
    echo "❌ Error: Unknown method: $METHOD"
    echo "Valid methods: restart, helm-upgrade, annotation"
    exit 1
    ;;
esac

echo "✅ Deployment $DEPLOYMENT_NAME updated successfully!"




