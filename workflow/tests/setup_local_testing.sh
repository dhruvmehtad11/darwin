#!/bin/bash

# Local Testing Setup Script for Workflow
# This script sets up the local environment for testing workflows

set -e

echo "=========================================="
echo "Setting up Local Testing Environment"
echo "=========================================="

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. Create FSX workspace directory structure
echo -e "${YELLOW}Step 1: Creating FSX workspace directories...${NC}"
sudo mkdir -p /var/www/fsx/workspace/test
sudo mkdir -p /home/ray/fsx/workspace/test
sudo chmod -R 777 /var/www/fsx/
sudo chmod -R 777 /home/ray/fsx/ 2>/dev/null || echo "Note: /home/ray/fsx/ may not exist yet (will be created by Ray)"

echo -e "${GREEN}✓ FSX directories created${NC}"

# 2. Create a simple test Python script
echo -e "${YELLOW}Step 2: Creating test Python script...${NC}"
TEST_SCRIPT="/var/www/fsx/workspace/test/test_script.py"
cat > "$TEST_SCRIPT" << 'EOF'
#!/usr/bin/env python3
"""
Simple test script for workflow testing
This script runs every 10 minutes when scheduled
"""
import os
import sys
from datetime import datetime

def main():
    print("=" * 50)
    print(f"Test Workflow Script - Running at {datetime.now()}")
    print("=" * 50)
    
    # Get input parameters if provided
    test_param = os.environ.get('test_param', 'default_value')
    print(f"Test Parameter: {test_param}")
    
    # Simulate some work
    print("Processing...")
    import time
    time.sleep(2)
    
    print("Work completed successfully!")
    print("=" * 50)
    return 0

if __name__ == "__main__":
    sys.exit(main())
EOF

chmod +x "$TEST_SCRIPT"
echo -e "${GREEN}✓ Test script created at $TEST_SCRIPT${NC}"

# 3. Create a test script with workspace source
echo -e "${YELLOW}Step 3: Creating workspace test structure...${NC}"
WORKSPACE_TEST_DIR="/var/www/fsx/workspace/workspace://test"
sudo mkdir -p "$WORKSPACE_TEST_DIR"
sudo cp "$TEST_SCRIPT" "$WORKSPACE_TEST_DIR/test_script.py"
sudo chmod -R 777 "$WORKSPACE_TEST_DIR"
echo -e "${GREEN}✓ Workspace test structure created${NC}"

# 4. Display environment variables needed
echo -e "${YELLOW}Step 4: Environment setup...${NC}"
echo ""
echo "Make sure you have these environment variables set:"
echo "  export ENV=local"
echo "  export VAULT_SERVICE_GITHUB_TOKEN='your_github_token'  # Optional, for private repos"
echo ""

# 5. Display test workflow JSON files
echo -e "${YELLOW}Step 5: Test workflow files...${NC}"
echo ""
echo "Test workflow JSON files created:"
echo "  - workflow/tests/local_test_workflow.json (V2 API)"
echo "  - workflow/tests/local_test_workflow_v3.json (V3 API)"
echo ""

# 6. Display usage instructions
echo -e "${YELLOW}Step 6: Usage Instructions${NC}"
echo ""
echo "To test the workflow creation API:"
echo ""
echo "1. Start the workflow service (if not already running):"
echo "   cd workflow/app_layer"
echo "   uvicorn workflow_app_layer.main:app --host localhost --port 8000 --reload"
echo ""
echo "2. Create workflow using V2 API:"
echo "   curl -X POST http://localhost:8000/v2/workflow \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -H 'msd-user: {\"id\":5513,\"email\":\"test@dream11.com\"}' \\"
echo "     -d @workflow/tests/local_test_workflow.json"
echo ""
echo "3. Create workflow using V3 API:"
echo "   curl -X POST http://localhost:8000/v3/workflow \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -H 'msd-user: {\"email\":\"test@dream11.com\"}' \\"
echo "     -d @workflow/tests/local_test_workflow_v3.json"
echo ""
echo "4. Check workflow status:"
echo "   curl http://localhost:8000/v2/workflow/{workflow_id} \\"
echo "     -H 'msd-user: {\"id\":5513,\"email\":\"test@dream11.com\"}'"
echo ""

echo -e "${GREEN}=========================================="
echo "Local Testing Environment Setup Complete!"
echo "==========================================${NC}"



