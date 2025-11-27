#!/usr/bin/env bash
set -e
ls -la
mkdir -p ./target/darwin-mlflow
cp -rf ./app_layer ./target/darwin-mlflow/app_layer
# Note: .odin directory is copied by image-builder.sh, not here

ls -la ./target/darwin-mlflow