#!/bin/bash

CLUSTER_NAME=$CLUSTER_NAME
KIND_CONFIG=$KIND_CONFIG
KUBECONFIG=$KUBECONFIG

if ! command -v kind &> /dev/null; then
    echo "kind could not be found, installing it"
    brew install kind || apt-get install kind
fi

export KUBECONFIG=$KUBECONFIG

# Install kuberay operator
echo "🚀 Installing kuberay-operator..."
helm repo add kuberay https://ray-project.github.io/kuberay-helm/
helm upgrade --install kuberay-operator kuberay/kuberay-operator --version 1.1.0 -n ray-system --create-namespace
kubectl wait --for=condition=Available --timeout=120s deployment/kuberay-operator -n ray-system
echo "✅ kuberay-operator installed successfully"

# Install nginx-proxy-server
echo "🚀 Installing nginx-proxy-server..."
helm upgrade --install nginx-proxy-server ./helm/nginx-proxy-server -n ray-system --create-namespace
kubectl wait --for=condition=Available --timeout=120s deployment/nginx-proxy-server -n ray-system
echo "✅ nginx-proxy-server installed successfully"

# Install kube-prometheus-stack
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm upgrade --install kube-prometheus-stack prometheus-community/kube-prometheus-stack --values ./helm/kube-prometheus-stack/values.yaml -n prometheus-system --create-namespace
kubectl apply -f ./helm/kube-prometheus-stack/prometheus-rbac.yaml

# Create service account with RBAC permissions
echo "🚀 Creating service account..."
kubectl create namespace ray 2>/dev/null || echo "Namespace already exists"
kubectl create serviceaccount darwin-ds-role -n ray 2>/dev/null || echo "Service account already exists"

# Install PV Chart
helm upgrade --install pv-chart ./helm/pv-chart -n ray
