# Install nfs client for mounting EFS
if [[ "$ENV" == "darwin-local" ]]; then
  echo "Skipping nfs-common installation for local environment"
else
  apt install nfs-common -y || echo "⚠️  nfs-common not available, skipping"
fi

echo "Printing env variables"

printenv

if [[ "$DEPLOYMENT_TYPE" == "aws_ec2" ]] || [[ "$DEPLOYMENT_TYPE" == "container" ]]; then
  cd "${APP_DIR}"
  echo "installing dependencies..."
  # Install workflow application packages if app_layer and core directories exist
  if [ -d "${BASE_DIR}/app_layer" ] && [ -d "${BASE_DIR}/core" ]; then
    # Create static directories for workflow (if they don't exist)
    mkdir -p ${BASE_DIR}/app_layer/src/workflow_app_layer/static 2>/dev/null || true
    mkdir -p ${BASE_DIR}/static 2>/dev/null || true
    
    # Install dependencies from app_layer/requirements.txt (excluding -e ../core)
    echo "📦 Installing darwin-workflow app_layer dependencies..."
    grep -v "^-e" ${BASE_DIR}/app_layer/requirements.txt | pip3 install -r /dev/stdin || exit 1
    echo "✅ App layer dependencies installed successfully"
    
    # Install dependencies from core/requirements.txt (excluding compute_sdk which needs special handling)
    echo "📦 Installing darwin-workflow core dependencies..."
    if [ -f "${BASE_DIR}/core/requirements.txt" ]; then
      # Filter out compute_sdk from requirements.txt and install the rest
      grep -v -E "^(compute_sdk|darwin-compute)" ${BASE_DIR}/core/requirements.txt | pip3 install -r /dev/stdin || exit 1      
      # Build and install compute_sdk from local source
      echo "📦 Building and installing compute_sdk from local source..."
      if [ -d "${BASE_DIR}/darwin-compute/sdk" ]; then
        # Install darwin-compute/core first (dependency for model and SDK)
        if [ -d "${BASE_DIR}/darwin-compute/core" ]; then
          echo "  Installing darwin-compute/core..."
          pip3 install -e ${BASE_DIR}/darwin-compute/core/. --force-reinstall || {
            echo "⚠️  Failed to install darwin-compute/core, continuing anyway..."
          }
        fi
        
        # Install darwin-compute/model (dependency for SDK, and it depends on core)
        if [ -d "${BASE_DIR}/darwin-compute/model" ]; then
          echo "  Installing darwin-compute/model..."
          pip3 install -e ${BASE_DIR}/darwin-compute/model/. --force-reinstall || {
            echo "⚠️  Failed to install darwin-compute/model, continuing anyway..."
          }
          
          # Prepare SDK package structure: copy model source to SDK src directory
          echo "  Preparing SDK package structure..."
          SDK_SRC_DIR="${BASE_DIR}/darwin-compute/sdk/src"
          MODEL_SRC_DIR="${BASE_DIR}/darwin-compute/model/src/compute_model"
          SDK_MODEL_DIR="${SDK_SRC_DIR}/compute_model"
          if [ -d "$MODEL_SRC_DIR" ] && [ ! -d "$SDK_MODEL_DIR" ]; then
            echo "  Copying compute_model to SDK src directory..."
            cp -r "$MODEL_SRC_DIR" "$SDK_MODEL_DIR"
          fi
        fi
        
        # Install darwin-compute SDK
        echo "  Installing darwin-compute SDK..."
        if pip3 install -e ${BASE_DIR}/darwin-compute/sdk/. --force-reinstall; then
          echo "✅ darwin-compute SDK installed successfully"
          
          # Create compute_sdk namespace alias since code imports from compute_sdk
          echo "  Creating compute_sdk namespace alias..."
          PYTHON_SITE_PACKAGES=$(python3 -c "import site; print(site.getsitepackages()[0])")
          COMPUTE_SDK_DIR="${PYTHON_SITE_PACKAGES}/compute_sdk"
          mkdir -p "$COMPUTE_SDK_DIR"
          cat > "${COMPUTE_SDK_DIR}/__init__.py" << 'EOF'
import sys
# Import everything from darwin_compute
from darwin_compute import *
from darwin_compute.compute import ComputeCluster
# Create module aliases for compute_sdk namespace
sys.modules['compute_sdk'] = sys.modules['darwin_compute']
sys.modules['compute_sdk.compute'] = sys.modules['darwin_compute.compute']
EOF
          echo "✅ Created compute_sdk namespace alias"
        else
          echo "❌ ERROR: Failed to install darwin-compute SDK"
          exit 1
        fi
      else
        echo "❌ ERROR: darwin-compute/sdk not found in build context"
        echo "   Expected location: ${BASE_DIR}/darwin-compute/sdk"
        echo "   Make sure build.sh copies darwin-compute/sdk to the target directory"
        exit 1
      fi
      echo "✅ Core dependencies installed successfully"
    fi
    
    echo "📦 Installing darwin-workflow packages (using --no-deps)..."
    # Install commons package first (it's a dependency for core and app_layer)
    if [ -d "${BASE_DIR}/commons" ]; then
      pip3 install -e ${BASE_DIR}/commons/. --no-deps --force-reinstall || exit 1
      echo "✅ Commons package installed"
    fi
    pip3 install -e ${BASE_DIR}/core/. --no-deps --force-reinstall || exit 1
    pip3 install -e ${BASE_DIR}/app_layer/. --no-deps --force-reinstall || exit 1
    if [ -d "${BASE_DIR}/model" ]; then
      pip3 install -e ${BASE_DIR}/model/. --no-deps --force-reinstall || exit 1
    fi
    echo "✅ Darwin-workflow packages installed"
  else
    echo "Installing local packages..."

    # Install dependencies from app_layer/requirements.txt (excluding -e ../core)
    echo "📦 Installing app_layer dependencies..."
    if [ -f "app_layer/requirements.txt" ]; then
      grep -v "^-e" app_layer/requirements.txt | pip3 install -r /dev/stdin || exit 1
      echo "✅ App layer dependencies installed successfully"
    fi

    # Install dependencies from core/requirements.txt (excluding compute_sdk which needs special handling)
    echo "📦 Installing core dependencies..."
    if [ -f "core/requirements.txt" ]; then
      # Filter out compute_sdk from requirements.txt and install the rest
      grep -v "^compute_sdk" core/requirements.txt | pip3 install -r /dev/stdin || exit 1
      
      # Build and install compute_sdk from local source
      echo "📦 Building and installing compute_sdk from local source..."
      if [ -d "darwin-compute/sdk" ]; then
        # Install darwin-compute/core first (dependency for model and SDK)
        if [ -d "darwin-compute/core" ]; then
          echo "  Installing darwin-compute/core..."
          pip3 install -e darwin-compute/core/. --force-reinstall || {
            echo "⚠️  Failed to install darwin-compute/core, continuing anyway..."
          }
        fi
        
        # Install darwin-compute/model (dependency for SDK, and it depends on core)
        if [ -d "darwin-compute/model" ]; then
          echo "  Installing darwin-compute/model..."
          pip3 install -e darwin-compute/model/. --force-reinstall || {
            echo "⚠️  Failed to install darwin-compute/model, continuing anyway..."
          }
          
          # Prepare SDK package structure: copy model source to SDK src directory
          echo "  Preparing SDK package structure..."
          SDK_SRC_DIR="darwin-compute/sdk/src"
          MODEL_SRC_DIR="darwin-compute/model/src/compute_model"
          SDK_MODEL_DIR="${SDK_SRC_DIR}/compute_model"
          if [ -d "$MODEL_SRC_DIR" ] && [ ! -d "$SDK_MODEL_DIR" ]; then
            echo "  Copying compute_model to SDK src directory..."
            cp -r "$MODEL_SRC_DIR" "$SDK_MODEL_DIR"
          fi
        fi
        
        # Install darwin-compute SDK
        echo "  Installing darwin-compute SDK..."
        if pip3 install -e darwin-compute/sdk/. --force-reinstall; then
          echo "✅ darwin-compute SDK installed successfully"
          
          # Create compute_sdk namespace alias since code imports from compute_sdk
          echo "  Creating compute_sdk namespace alias..."
          PYTHON_SITE_PACKAGES=$(python3 -c "import site; print(site.getsitepackages()[0])")
          COMPUTE_SDK_DIR="${PYTHON_SITE_PACKAGES}/compute_sdk"
          mkdir -p "$COMPUTE_SDK_DIR"
          cat > "${COMPUTE_SDK_DIR}/__init__.py" << 'EOF'
import sys
# Import everything from darwin_compute
from darwin_compute import *
from darwin_compute.compute import ComputeCluster
# Create module aliases for compute_sdk namespace
sys.modules['compute_sdk'] = sys.modules['darwin_compute']
sys.modules['compute_sdk.compute'] = sys.modules['darwin_compute.compute']
EOF
          echo "✅ Created compute_sdk namespace alias"
        else
          echo "❌ ERROR: Failed to install darwin-compute SDK"
          exit 1
        fi
      else
        echo "❌ ERROR: darwin-compute/sdk not found"
        echo "   Expected location: darwin-compute/sdk"
        echo "   Make sure build.sh copies darwin-compute/sdk to the target directory"
        exit 1
      fi
      echo "✅ Core dependencies installed successfully"
    fi

    # Install commons package first (it's a dependency for core and app_layer)
    echo "Installing commons package..."
    if [ -d "commons" ]; then
      if pip3 install -e commons/. --no-deps --force-reinstall; then
        echo "✅ Commons package installed successfully"
      else
        echo "❌ Failed to install commons packages"
        exit 1
      fi
    fi

    # Install core package first (without dependencies)
    echo "Installing core package..."
    if pip3 install -e core/. --no-deps --force-reinstall; then
      echo "✅ Core package installed successfully"
    else
      echo "❌ Failed to install core package"
      exit 1
    fi

    # Install model package
    echo "Installing model package..."
    if pip3 install -e model/. --no-deps --force-reinstall; then
      echo "✅ Model package installed successfully"
    else
      echo "❌ Failed to install model package"
      exit 1
    fi

    # Install app_layer package
    echo "Installing app_layer package..."
    if pip3 install -e app_layer/. --no-deps --force-reinstall; then
      echo "✅ App layer package installed successfully"
    else
      echo "❌ Failed to install app_layer package"
      exit 1
    fi

    echo "✅ All requirements installed successfully"
  fi
else
  echo "$DEPLOYMENT_TYPE"
fi
echo "Requirements installed"

pip3 list