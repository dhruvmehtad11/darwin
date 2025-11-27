#!/bin/bash

# Script to inspect LocalStack contents in the Darwin cluster
# Usage: ./inspect_localstack.sh [command]
# Commands: health, buckets, list-all, port-forward, shell

set -e

KUBECONFIG="${KUBECONFIG:-./kind/config/kindkubeconfig.yaml}"
NAMESPACE="darwin"
LOCALSTACK_POD=$(kubectl --kubeconfig="$KUBECONFIG" get pods -n "$NAMESPACE" -l app.kubernetes.io/component=localstack -o jsonpath='{.items[0].metadata.name}')
LOCALSTACK_ENDPOINT="http://localhost:4566"

echo "LocalStack Pod: $LOCALSTACK_POD"
echo "=================================="
echo ""

case "${1:-help}" in
  health)
    echo "Checking LocalStack health..."
    kubectl --kubeconfig="$KUBECONFIG" exec -n "$NAMESPACE" "$LOCALSTACK_POD" -- \
      curl -s http://localhost:4566/_localstack/health | jq '.' || \
      kubectl --kubeconfig="$KUBECONFIG" exec -n "$NAMESPACE" "$LOCALSTACK_POD" -- \
      curl -s http://localhost:4566/_localstack/health
    ;;
  
  buckets)
    echo "Listing S3 buckets..."
    kubectl --kubeconfig="$KUBECONFIG" exec -n "$NAMESPACE" "$LOCALSTACK_POD" -- \
      aws --endpoint-url=http://localhost:4566 s3 ls || \
      echo "AWS CLI not available in pod. Use 'port-forward' method instead."
    ;;
  
  list-all)
    echo "Listing all S3 buckets and their contents..."
    kubectl --kubeconfig="$KUBECONFIG" exec -n "$NAMESPACE" "$LOCALSTACK_POD" -- \
      sh -c 'aws --endpoint-url=http://localhost:4566 s3 ls && echo "" && for bucket in $(aws --endpoint-url=http://localhost:4566 s3 ls | awk "{print \$3}"); do echo "=== Bucket: $bucket ==="; aws --endpoint-url=http://localhost:4566 s3 ls s3://$bucket --recursive || true; echo ""; done' || \
      echo "AWS CLI not available in pod. Use 'port-forward' method instead."
    ;;
  
  port-forward)
    echo "Starting port-forward to LocalStack on port 4566..."
    echo "Access LocalStack at: http://localhost:4566"
    echo "In another terminal, you can now run:"
    echo "  export AWS_ENDPOINT_URL=http://localhost:4566"
    echo "  aws --endpoint-url=http://localhost:4566 s3 ls"
    echo ""
    echo "Press Ctrl+C to stop port-forwarding..."
    kubectl --kubeconfig="$KUBECONFIG" port-forward -n "$NAMESPACE" "$LOCALSTACK_POD" 4566:4566
    ;;
  
  shell)
    echo "Opening shell in LocalStack pod..."
    kubectl --kubeconfig="$KUBECONFIG" exec -n "$NAMESPACE" "$LOCALSTACK_POD" -it -- /bin/sh
    ;;
  
  aws-cli)
    if ! command -v aws &> /dev/null; then
      echo "Error: AWS CLI is not installed."
      echo "Install it with: brew install awscli"
      exit 1
    fi
    
    # Start port-forward in background
    echo "Starting port-forward in background..."
    kubectl --kubeconfig="$KUBECONFIG" port-forward -n "$NAMESPACE" "$LOCALSTACK_POD" 4566:4566 > /dev/null 2>&1 &
    PF_PID=$!
    sleep 2
    
    echo "Using AWS CLI to inspect LocalStack..."
    echo ""
    
    echo "=== S3 Buckets ==="
    aws --endpoint-url=http://localhost:4566 s3 ls || echo "No buckets found"
    echo ""
    
    echo "=== Listing all objects ==="
    for bucket in $(aws --endpoint-url=http://localhost:4566 s3 ls 2>/dev/null | awk '{print $3}'); do
      echo "Bucket: $bucket"
      aws --endpoint-url=http://localhost:4566 s3 ls "s3://$bucket" --recursive --human-readable --summarize 2>/dev/null || echo "  (empty or error)"
      echo ""
    done
    
    # Cleanup
    kill $PF_PID 2>/dev/null || true
    ;;
  
  help|*)
    echo "LocalStack Inspector"
    echo ""
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  health        - Check LocalStack health status"
    echo "  buckets       - List all S3 buckets (requires AWS CLI in pod)"
    echo "  list-all      - List all buckets and their contents (requires AWS CLI in pod)"
    echo "  port-forward  - Start port-forward to access LocalStack locally"
    echo "  aws-cli       - Use local AWS CLI to inspect LocalStack (recommended)"
    echo "  shell         - Open interactive shell in LocalStack pod"
    echo ""
    echo "Examples:"
    echo "  $0 health              # Check if LocalStack is healthy"
    echo "  $0 aws-cli             # List all buckets and objects using local AWS CLI"
    echo "  $0 port-forward        # Forward port 4566 to access from local machine"
    echo ""
    echo "After port-forwarding, in another terminal:"
    echo "  export AWS_ENDPOINT_URL=http://localhost:4566"
    echo "  aws --endpoint-url=http://localhost:4566 s3 ls"
    echo "  aws --endpoint-url=http://localhost:4566 s3 ls s3://your-bucket-name --recursive"
    ;;
esac

