import os
from typing import Dict, Any, Optional

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field, model_validator

from src.api_client import APIClient
from src.config.config import Config
from src.feature_store.feature_store import FeatureStoreClient
from src.model.model import Model
from src.model.model_loader.ml_flow_model_loader import MLFlowModelLoader


class PredictRequest(BaseModel):
    """
    Request model for prediction endpoint.
    
    Supports two modes:
    
    Mode 1 - Feature Store (OFS) Mode:
        Provide feature_group and ofs_keys to fetch features from the feature store.
        
    Mode 2 - Direct Features Mode:
        Provide features dictionary directly to bypass feature store.
    
    Attributes:
        feature_group: (Optional) Name of the feature group to fetch features from
        ofs_keys: (Optional) Dictionary containing primary keys for feature lookup
        features: (Optional) Dictionary containing feature names and values directly
        
    Examples:
        Mode 1 - Using Feature Store:
        {
            "feature_group": "serve_test_fg",
            "ofs_keys": {"user_id": 1}
        }
        
        Mode 2 - Direct Features:
        {
            "features": {
                "airconditioning": 0,
                "area": 4640,
                "basement": 0,
                "bathrooms": 1,
                "bedrooms": 4,
                "furnishingstatus": 1,
                "guestroom": 0,
                "hotwaterheating": 0,
                "mainroad": 1,
                "parking": 1,
                "prefarea": 0,
                "stories": 2
            }
        }
    """
    feature_group: Optional[str] = Field(None, description="Name of the feature group (for OFS mode)")
    ofs_keys: Optional[Dict[str, Any]] = Field(None, description="Primary keys for feature lookup (for OFS mode)")
    features: Optional[Dict[str, Any]] = Field(None, description="Feature dictionary for direct prediction (bypass OFS)")

    @model_validator(mode='after')
    def validate_mode(self):
        """Validate that either (feature_group + ofs_keys) OR features is provided"""
        has_ofs_mode = self.feature_group is not None and self.ofs_keys is not None
        has_direct_mode = self.features is not None
        
        if not has_ofs_mode and not has_direct_mode:
            raise ValueError(
                "Either provide (feature_group + ofs_keys) for OFS mode, "
                "or provide (features) for direct prediction mode"
            )
        
        if has_ofs_mode and has_direct_mode:
            raise ValueError(
                "Cannot use both modes simultaneously. "
                "Either provide (feature_group + ofs_keys) OR (features), not both"
            )
        
        if self.feature_group is not None and self.ofs_keys is None:
            raise ValueError("feature_group requires ofs_keys")
        
        if self.ofs_keys is not None and self.feature_group is None:
            raise ValueError("ofs_keys requires feature_group")
        
        return self


class PredictResponse(BaseModel):
    """Response model for prediction endpoint"""
    prediction: Any = Field(..., description="Model prediction result")

# ROOT_PATH is used for proper OpenAPI/Swagger docs when behind a reverse proxy
# e.g., if app is served at /my-model/, set ROOT_PATH=/my-model
root_path = os.environ.get("ROOT_PATH", "")
app = FastAPI(root_path=root_path)

config = Config()

model_loader = MLFlowModelLoader(config=config)

api_client = APIClient(config=config)

feature_store_client = FeatureStoreClient(api_client=api_client, config=config)

model = Model(model_loader=model_loader)


@app.get("/healthcheck")
async def healthcheck():
    """Health check endpoint to verify service is running"""
    return {"status": "healthy"}


@app.post("/predict", response_model=Dict[str, Any])
async def predict(request: PredictRequest):
    """
    Make predictions using the loaded model.
    
    Supports two modes:
    
    **Mode 1 - Feature Store (OFS) Mode:**
    - Provide feature_group and ofs_keys
    - System fetches features from feature store
    - Steps:
      1. Fetches feature group metadata to get all available columns
      2. Determines which features need to be fetched (excludes the primary keys provided)
      3. Fetches the features from the feature store using the provided primary keys
      4. Makes a prediction using the model
    
    **Mode 2 - Direct Features Mode:**
    - Provide features dictionary directly
    - Bypasses feature store completely
    - Steps:
      1. Uses provided features dictionary directly
      2. Makes a prediction using the model
    
    Args:
        request: PredictRequest containing either:
                 - (feature_group + ofs_keys) for OFS mode, OR
                 - (features) for direct mode
        
    Returns:
        Dictionary containing the prediction result
        
    Raises:
        HTTPException: If prediction fails or validation errors occur
    """
    try:
        # Mode 2: Direct Features Mode (bypass OFS)
        if request.features is not None:
            feature_dict = request.features
        
        # Mode 1: Feature Store (OFS) Mode
        else:
            # Fetch feature group metadata to get all columns
            fg_columns = await feature_store_client.fetch_feature_meta_data(
                feature_group_name=request.feature_group
            )

            # Calculate which features need to be fetched (exclude primary keys)
            feature_columns = list(set(fg_columns) - request.ofs_keys.keys())

            # Fetch features from feature store
            features: Dict = await feature_store_client.fetch_feature_group_data(
                feature_group_name=request.feature_group,
                feature_columns=feature_columns,
                primary_key_names=list(request.ofs_keys.keys()),
                primary_key_values=[list(request.ofs_keys.values())],
            )

            # Combine fetched features with provided keys
            feature_dict = dict(zip(feature_columns, features))

        # Make prediction (same for both modes)
        result = await model.predict(feature_dict)

        return result
    
    except ValueError as e:
        # Validation errors
        raise HTTPException(
            status_code=422,
            detail=str(e)
        )
    except Exception as e:
        # Other errors
        raise HTTPException(
            status_code=500,
            detail=f"Prediction failed: {str(e)}"
        )
