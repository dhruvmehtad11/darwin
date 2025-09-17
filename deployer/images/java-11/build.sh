#!/bin/sh
set -e

mkdir -p tmp
cp $HOME/.m2/settings.xml tmp/settings.xml

docker build \
  --build-arg JAVA_DOWNLOAD_URL_AMD64="https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.25%2B9/OpenJDK11U-jre_x64_linux_hotspot_11.0.25_9.tar.gz" \
  --build-arg JAVA_DOWNLOAD_URL_ARM64="https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.25%2B9/OpenJDK11U-jre_aarch64_linux_hotspot_11.0.25_9.tar.gz" \
  -t darwin/java:11-maven-bookworm-slim \
  --load .

rm -r ./tmp
