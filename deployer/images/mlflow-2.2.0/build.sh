#!/bin/sh
set -e

docker build \
  -t darwin/mlflow:2.2.0 \
  --load .