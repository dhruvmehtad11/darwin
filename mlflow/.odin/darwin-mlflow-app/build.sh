#!/usr/bin/env bash
set -e
echo "Building darwin-mlflow-app"
ls -la
mkdir -p ./target/darwin-mlflow-app
cp -rf ./app_layer ./target/darwin-mlflow-app/app_layer

ls -la ./target/darwin-mlflow-app