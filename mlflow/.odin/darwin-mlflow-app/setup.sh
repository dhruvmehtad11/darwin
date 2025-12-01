#!/usr/bin/env bash
set -e

# Set default values if environment variables are not set
BASE_DIR=${BASE_DIR:-/app}
SERVICE_NAME=${SERVICE_NAME}
DEPLOYMENT_TYPE=${DEPLOYMENT_TYPE:-container}
APP_DIR=${APP_DIR:-/app}

if [[ -z "$SERVICE_NAME" ]]; then
  echo "SERVICE_NAME is not set"
  exit 1
fi

echo "BASE_DIR: $BASE_DIR"
echo "SERVICE_NAME: $SERVICE_NAME" 
echo "DEPLOYMENT_TYPE: $DEPLOYMENT_TYPE"
echo "APP_DIR: $APP_DIR"

# This is to install git in the containers for the deployment type container
if [[ "$DEPLOYMENT_TYPE" == "container" ]]; then
  echo "Installing git and setting up Python environment..."
  ls -la
  echo "Creating Python virtual environment..."
  python3 -m venv .
  source bin/activate
  bin/python3 -m pip install --upgrade pip
  export PATH=$PATH:"$BASE_DIR"/"$SERVICE_NAME"/bin

  echo "Installing app_layer package..."
  if [ -d "app_layer" ]; then
    bin/pip3 install -e app_layer/.
    bin/pip3 install urllib3==1.26.6 --force-reinstall
    pip install boto3 --force-reinstall
    # Ensure uvicorn is available
    bin/pip3 install uvicorn==0.23.2
    uvicorn --version
    echo "Requirements installed successfully"
  else
    echo "ERROR: app_layer directory not found in $BASE_DIR/$SERVICE_NAME"
    echo "Contents of current directory:"
    ls -la
    exit 1
  fi
else
  echo "Changing to directory: $BASE_DIR/$SERVICE_NAME"
  cd "$BASE_DIR"/"$SERVICE_NAME" || { echo "Failed to change to $BASE_DIR/$SERVICE_NAME"; exit 1; }
  
  echo "Installing app_layer package..."
  if [ -d "app_layer" ]; then
    pip3 install -e app_layer/. --force-reinstall
    pip3 install urllib3==1.26.6 --force-reinstall
    pip3 install boto3 --force-reinstall
    pip3 install uvicorn==0.23.2
    pip install "mlflow[auth]==2.12.2" --force-reinstall
    uvicorn --version
    echo "Requirements installed successfully"
  else
    echo "ERROR: app_layer directory not found in $BASE_DIR/$SERVICE_NAME"
    echo "Contents of current directory:"
    ls -la
    exit 1
  fi
fi