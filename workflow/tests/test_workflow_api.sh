#!/bin/bash

# Test script for Workflow Create API
# This script validates JSON files and tests the API endpoints

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="http://localhost/workflow"
V2_HEADERS=(-H 'Content-Type: application/json' -H 'msd-user: {"id":5513,"email":"test@dream11.com"}')
V3_HEADERS=(-H 'Content-Type: application/json' -H 'msd-user: {"email":"test@dream11.com"}')

echo "=========================================="
echo "Testing Workflow Create API"
echo "=========================================="
echo ""

# Function to check if service is running
check_service() {
    echo -e "${YELLOW}Checking if workflow service is running...${NC}"
    if curl -s --connect-timeout 2 "$BASE_URL/healthcheck" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Service is running${NC}"
        return 0
    else
        echo -e "${RED}✗ Service is not running${NC}"
        echo ""
        echo "To start the service, run:"
        echo "  cd workflow/app_layer"
        echo "  export ENV=local"
        echo "  uvicorn workflow_app_layer.main:app --host localhost --port 8000 --reload"
        echo ""
        return 1
    fi
}

# Function to validate JSON
validate_json() {
    local json_file=$1
    echo -e "${YELLOW}Validating JSON: $json_file${NC}"
    
    if [ ! -f "$json_file" ]; then
        echo -e "${RED}✗ File not found: $json_file${NC}"
        return 1
    fi
    
    if python3 -m json.tool "$json_file" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ JSON is valid${NC}"
        
        # Display key fields
        echo "  Workflow Name: $(python3 -c "import json; print(json.load(open('$json_file'))['workflow_name'])" 2>/dev/null || echo 'N/A')"
        echo "  Schedule: $(python3 -c "import json; print(json.load(open('$json_file'))['schedule'])" 2>/dev/null || echo 'N/A')"
        echo "  Tasks: $(python3 -c "import json; print(len(json.load(open('$json_file'))['tasks']))" 2>/dev/null || echo 'N/A')"
        return 0
    else
        echo -e "${RED}✗ Invalid JSON${NC}"
        return 1
    fi
}

# Function to test V2 API
test_v2_api() {
    local json_file=$1
    echo ""
    echo -e "${YELLOW}Testing V2 API with: $json_file${NC}"
    
    response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/v2/workflow" \
        "${V2_HEADERS[@]}" \
        --data-binary @"$json_file" 2>&1)
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ]; then
        echo -e "${GREEN}✓ V2 API test successful (HTTP $http_code)${NC}"
        echo "Response:"
        echo "$body" | python3 -m json.tool 2>/dev/null || echo "$body"
        
        # Extract workflow_id if available
        workflow_id=$(echo "$body" | python3 -c "import json, sys; data=json.load(sys.stdin); print(data.get('data', {}).get('workflow_id', 'N/A'))" 2>/dev/null || echo "N/A")
        if [ "$workflow_id" != "N/A" ] && [ "$workflow_id" != "" ]; then
            echo ""
            echo "Workflow ID: $workflow_id"
            echo "Check status: curl $BASE_URL/v2/workflow/$workflow_id ${V2_HEADERS[@]}"
        fi
        return 0
    else
        echo -e "${RED}✗ V2 API test failed (HTTP $http_code)${NC}"
        echo "Response:"
        echo "$body" | python3 -m json.tool 2>/dev/null || echo "$body"
        return 1
    fi
}

# Function to test V3 API
test_v3_api() {
    local json_file=$1
    echo ""
    echo -e "${YELLOW}Testing V3 API with: $json_file${NC}"
    
    response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/v3/workflow" \
        "${V3_HEADERS[@]}" \
        --data-binary @"$json_file" 2>&1)
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ]; then
        echo -e "${GREEN}✓ V3 API test successful (HTTP $http_code)${NC}"
        echo "Response:"
        echo "$body" | python3 -m json.tool 2>/dev/null || echo "$body"
        
        # Extract workflow_id if available
        workflow_id=$(echo "$body" | python3 -c "import json, sys; data=json.load(sys.stdin); print(data.get('data', {}).get('workflow_id', 'N/A'))" 2>/dev/null || echo "N/A")
        if [ "$workflow_id" != "N/A" ] && [ "$workflow_id" != "" ]; then
            echo ""
            echo "Workflow ID: $workflow_id"
            echo "Check status: curl $BASE_URL/v3/workflow/$workflow_id ${V3_HEADERS[@]}"
        fi
        return 0
    else
        echo -e "${RED}✗ V3 API test failed (HTTP $http_code)${NC}"
        echo "Response:"
        echo "$body" | python3 -m json.tool 2>/dev/null || echo "$body"
        return 1
    fi
}

# Main execution
main() {
    # Check if we're in the right directory
    if [ ! -f "local_test_workflow.json" ]; then
        echo -e "${RED}✗ Error: local_test_workflow.json not found${NC}"
        echo "Please run this script from the workflow/tests directory"
        exit 1
    fi
    
    # Validate JSON files
    echo "Step 1: Validating JSON files"
    echo "----------------------------------------"
    validate_json "local_test_workflow.json"
    validate_json "local_test_workflow_v3.json"
    echo ""
    
    # Check service
    echo "Step 2: Checking service status"
    echo "----------------------------------------"
    if ! check_service; then
        echo ""
        echo "=========================================="
        echo "JSON files are valid, but service is not running"
        echo "Start the service and run this script again"
        echo "=========================================="
        exit 0
    fi
    echo ""
    
    # Test APIs
    echo "Step 3: Testing API endpoints"
    echo "----------------------------------------"
    
    # Test V2 API
    if validate_json "local_test_workflow.json"; then
        test_v2_api "local_test_workflow.json"
    fi
    
    # Test V3 API
    if validate_json "local_test_workflow_v3.json"; then
        test_v3_api "local_test_workflow_v3.json"
    fi
    
    echo ""
    echo "=========================================="
    echo "Testing complete!"
    echo "=========================================="
}

# Run main function
main "$@"

