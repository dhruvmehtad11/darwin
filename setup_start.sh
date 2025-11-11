#!/usr/bin/env bash

set -x

if sh setup.sh; then
  helm uninstall darwin -n darwin || true
  sleep 10
  sh start.sh
else
  exit 1
fi