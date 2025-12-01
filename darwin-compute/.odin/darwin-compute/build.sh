#!/usr/bin/env bash

set -e

cp -rf ./app_layer ./target/darwin-compute/app_layer
cp -rf ./core ./target/darwin-compute/core
cp -rf ./model ./target/darwin-compute/model
cp -rf ./script ./target/darwin-compute/script
cp -rf ./resources ./target/darwin-compute/resources
