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

# Install black
printf "${GREEN}Installing black...${NC}\n"
pip3 install black

# Run black on the current directory
printf "${GREEN}Running black on the current directory...${NC}\n"
black .


