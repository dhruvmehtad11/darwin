#!/usr/bin/env bash

set -e

cd "$BASE_DIR" || exit

# Requirements installation
pip install -e app_layer/. --force-reinstall
pip install -e core/. --force-reinstall
pip install -e model/. --force-reinstall
pip install -e script/. --force-reinstall
pip install urllib3==1.26.6 --force-reinstall
echo "Requirements installed"
