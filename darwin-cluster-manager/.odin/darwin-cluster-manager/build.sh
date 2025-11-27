#!/usr/bin/env bash

set -e # For enabling Exit on error

cp -rf ./* ./target/darwin-cluster-manager/

# Copy the kind kubeconfig file to the target directory
mkdir -p ./target/darwin-cluster-manager/configs
cp ../kind/config/kindkubeconfig.yaml ./target/darwin-cluster-manager/configs/kind

# Update the kubeconfig server address to use the in-cluster DNS name
sed -i '' 's|server: https://127\.0\.0\.1:[0-9]*|server: https://kubernetes.default.svc|' ./target/darwin-cluster-manager/configs/kind
