#!/usr/bin/env python3
"""
Standalone script to ensure S3 bucket exists.
Can be run independently or as part of initialization.
"""
import os
import sys

# Add parent directory to path to import mlflow_app_layer
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "src"))

from mlflow_app_layer.util.s3_utils import initialize_s3_bucket

if __name__ == "__main__":
    success = initialize_s3_bucket()
    if success:
        print("✓ S3 bucket initialization successful")
        sys.exit(0)
    else:
        print("✗ S3 bucket initialization failed")
        sys.exit(1)

