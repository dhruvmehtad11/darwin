#!/bin/bash

# Exit on any error
set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check for setup.py in current directory
if [ ! -f "setup.py" ]; then
    cd ..
    if [ ! -f "setup.py" ]; then
        printf "${RED}Error: setup.py not found in current or parent directory${NC}\n"
        exit 1
    fi
fi

# Check if virtual environment already exists
if [ ! -d ".venv" ]; then
    printf "${GREEN}Setting up Python virtual environment...${NC}\n"
    python3 -m venv .venv
else
    printf "${GREEN}Virtual environment already exists, skipping creation...${NC}\n"
fi

# Activate virtual environment
source .venv/bin/activate

printf "${GREEN}Installing package with all dependencies...${NC}\n"
pip3 install --upgrade pip wheel setuptools

# Install the package in editable mode with both dev and test extras
pip3 install --no-cache-dir --prefer-binary --upgrade -e .[test]

# Check if all requirements are installed
printf "${GREEN}Checking requirements...${NC}\n"
if ! pip freeze > /dev/null; then
    printf "${RED}Error: Some requirements are missing${NC}\n"
    exit 1
fi

printf "${GREEN}Starting uvicorn server...${NC}\n"
ENV=prod && uvicorn src.app_layer.main:app --host 0.0.0.0 --port 8080 --workers 2 --timeout-keep-alive 75

# Keep virtual environment active for the server
# Removed deactivate command since we need the environment for the server

printf "${GREEN}Setup completed and server is running!${NC}\n"
