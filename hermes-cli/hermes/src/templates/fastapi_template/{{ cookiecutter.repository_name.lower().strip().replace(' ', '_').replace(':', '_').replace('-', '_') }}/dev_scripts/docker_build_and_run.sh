#!/bin/bash

# Check for setup.py in current directory
if [ ! -f "Dockerfile" ]; then
    cd ..
    if [ ! -f "Dockerfile" ]; then
        printf "${RED}Error: Dockerfile not found in current or parent directory${NC}\n"
        exit 1
    fi
fi


# Build Docker image
echo "Building Docker image: {{ cookiecutter.repository_name.lower().strip().replace(' ', '_').replace(':', '_').replace('-', '_') }}"
docker build --platform linux/amd64 -t {{ cookiecutter.repository_name.lower().strip().replace(' ', '_').replace(':', '_').replace('-', '_') }} .

# Run Docker container
echo "Running Docker container from image: {{ cookiecutter.repository_name.lower().strip().replace(' ', '_').replace(':', '_').replace('-', '_') }}"
# Kill any process running on port 8000
lsof -ti:8000 | xargs kill -9 2>/dev/null || true

# Run Docker container with port mapping
docker run -it -p 8000:8000 {{ cookiecutter.repository_name.lower().strip().replace(' ', '_').replace(':', '_').replace('-', '_') }}
