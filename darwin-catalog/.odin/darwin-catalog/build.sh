#!/usr/bin/env bash
set -e

echo 'BUILDING ARTIFACT'
# Generate OpenAPI sources first, then build
echo 'Generating OpenAPI sources...'
mvn -U -B clean generate-sources
echo 'Compiling and packaging...'
mvn -U -B package -DskipTests
echo 'EXTRACTING VERSION'
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
echo "detected version $VERSION"
# Prepare output directories for local and container usage
mkdir -p ./target/catalog
mkdir -p ./target/darwin-catalog

# The jar is named darwin-catalog.jar based on finalName in pom.xml
# Local/dev layout (used by start.sh via APP_DIR=./target/catalog)
cp ./target/darwin-catalog.jar ./target/catalog/darwin-catalog-fat.jar
cp ./target/darwin-catalog.jar ./target/catalog/app.jar

# Container layout (used by Dockerfile: COPY .../target/${APP_NAME}/. /app/)
cp ./target/darwin-catalog.jar ./target/darwin-catalog/darwin-catalog-fat.jar
cp ./target/darwin-catalog.jar ./target/darwin-catalog/app.jar

echo 'CREATED ARTIFACT'