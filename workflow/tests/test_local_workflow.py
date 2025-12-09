#!/usr/bin/env python3
"""
Test script to create and manage local workflow with 10-minute cron schedule
"""
import requests
import json
import time
import sys
from datetime import datetime

# Configuration
BASE_URL = "http://localhost:8000"
HEADERS = {
    "Content-Type": "application/json",
    "msd-user": '{"id":5513,"email":"test@dream11.com"}'
}

V2_HEADERS = HEADERS.copy()
V3_HEADERS = {
    "Content-Type": "application/json",
    "msd-user": '{"email":"test@dream11.com"}'
}


def load_workflow_json(file_path):
    """Load workflow JSON from file"""
    with open(file_path, 'r') as f:
        return json.load(f)


def create_workflow_v2(workflow_data):
    """Create workflow using V2 API"""
    url = f"{BASE_URL}/v2/workflow"
    print(f"\nCreating workflow via V2 API: {workflow_data['workflow_name']}")
    response = requests.post(url, headers=V2_HEADERS, json=workflow_data)
    
    if response.status_code == 200:
        result = response.json()
        print(f"✓ Workflow created successfully!")
        print(f"  Workflow ID: {result['data']['workflow_id']}")
        print(f"  Workflow Name: {result['data']['workflow_name']}")
        return result['data']['workflow_id']
    else:
        print(f"✗ Failed to create workflow")
        print(f"  Status: {response.status_code}")
        print(f"  Response: {response.text}")
        return None


def create_workflow_v3(workflow_data):
    """Create workflow using V3 API"""
    url = f"{BASE_URL}/v3/workflow"
    print(f"\nCreating workflow via V3 API: {workflow_data['workflow_name']}")
    response = requests.post(url, headers=V3_HEADERS, json=workflow_data)
    
    if response.status_code == 200:
        result = response.json()
        print(f"✓ Workflow created successfully!")
        print(f"  Status: {result['status']}")
        if 'data' in result:
            print(f"  Workflow ID: {result['data'].get('workflow_id', 'N/A')}")
            print(f"  Workflow Name: {result['data'].get('workflow_name', 'N/A')}")
        return result.get('data', {}).get('workflow_id')
    else:
        print(f"✗ Failed to create workflow")
        print(f"  Status: {response.status_code}")
        print(f"  Response: {response.text}")
        return None


def get_workflow_status(workflow_id, api_version="v2"):
    """Get workflow status"""
    url = f"{BASE_URL}/{api_version}/workflow/{workflow_id}"
    headers = V2_HEADERS if api_version == "v2" else V3_HEADERS
    
    response = requests.get(url, headers=headers)
    if response.status_code == 200:
        return response.json()
    return None


def check_health():
    """Check if the workflow service is running"""
    try:
        response = requests.get(f"{BASE_URL}/healthcheck", timeout=5)
        if response.status_code == 200:
            print("✓ Workflow service is running")
            return True
    except requests.exceptions.RequestException:
        print("✗ Workflow service is not running")
        print("  Please start the service:")
        print("  cd workflow/app_layer")
        print("  uvicorn workflow_app_layer.main:app --host localhost --port 8000 --reload")
        return False


def main():
    print("=" * 60)
    print("Local Workflow Testing - 10 Minute Cron Schedule")
    print("=" * 60)
    
    # Check health
    if not check_health():
        sys.exit(1)
    
    # Ask user which API version to use
    print("\nWhich API version would you like to use?")
    print("1. V2 API (legacy)")
    print("2. V3 API (recommended)")
    choice = input("Enter choice (1 or 2): ").strip()
    
    if choice == "1":
        # V2 API
        workflow_file = "workflow/tests/local_test_workflow.json"
        try:
            workflow_data = load_workflow_json(workflow_file)
            workflow_id = create_workflow_v2(workflow_data)
            
            if workflow_id:
                print(f"\nWaiting 5 seconds before checking status...")
                time.sleep(5)
                status = get_workflow_status(workflow_id, "v2")
                if status:
                    print(f"\nWorkflow Status:")
                    print(json.dumps(status, indent=2))
        except FileNotFoundError:
            print(f"✗ Workflow file not found: {workflow_file}")
            sys.exit(1)
        except Exception as e:
            print(f"✗ Error: {e}")
            sys.exit(1)
    
    elif choice == "2":
        # V3 API
        workflow_file = "workflow/tests/local_test_workflow_v3.json"
        try:
            workflow_data = load_workflow_json(workflow_file)
            workflow_id = create_workflow_v3(workflow_data)
            
            if workflow_id:
                print(f"\nWaiting 5 seconds before checking status...")
                time.sleep(5)
                status = get_workflow_status(workflow_id, "v3")
                if status:
                    print(f"\nWorkflow Status:")
                    print(json.dumps(status, indent=2))
        except FileNotFoundError:
            print(f"✗ Workflow file not found: {workflow_file}")
            sys.exit(1)
        except Exception as e:
            print(f"✗ Error: {e}")
            sys.exit(1)
    else:
        print("✗ Invalid choice")
        sys.exit(1)
    
    print("\n" + "=" * 60)
    print("Test completed!")
    print("=" * 60)
    print("\nNote: The workflow is scheduled to run every 10 minutes (*/10 * * * *)")
    print("You can check the workflow runs in Airflow or via the API")


if __name__ == "__main__":
    main()



