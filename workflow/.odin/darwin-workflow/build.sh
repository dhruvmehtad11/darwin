#!/usr/bin/env bash

set -e

cp -rf ./app_layer ./target/darwin-workflow/app_layer
cp -rf ./core ./target/darwin-workflow/core
cp -rf ./model ./target/darwin-workflow/model
cp -rf ./commons ./target/darwin-workflow/commons
cp -rf ./.odin ./target/darwin-workflow/.odin

# Copy darwin-compute/sdk, model, and core for building compute_sdk from scratch
if [ -d "../darwin-compute/sdk" ]; then
  echo "📦 Copying darwin-compute packages for build..."
  mkdir -p ./target/darwin-workflow/darwin-compute
  cp -rf ../darwin-compute/sdk ./target/darwin-workflow/darwin-compute/sdk
  if [ -d "../darwin-compute/model" ]; then
    echo "📦 Copying darwin-compute/model for build..."
    cp -rf ../darwin-compute/model ./target/darwin-workflow/darwin-compute/model
  fi
  if [ -d "../darwin-compute/core" ]; then
    echo "📦 Copying darwin-compute/core for build..."
    cp -rf ../darwin-compute/core ./target/darwin-workflow/darwin-compute/core
  fi
else
  echo "⚠️  Warning: darwin-compute/sdk not found at ../darwin-compute/sdk"
  echo "   compute_sdk will not be available during build"
fi