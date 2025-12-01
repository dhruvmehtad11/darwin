# Sample YAML Configuration

This document provides an overview of the YAML configuration format for creating ML serving repositories with the Hermes CLI. The configuration is divided into several components, each serving a specific purpose in the deployment and operation of the service.

## Components

### 1. Repository Configuration (**REQUIRED**)
This section defines the basic metadata and type of the project.

- **repository**: Contains metadata about the project.
  - `name`: The name of the repository. **Required**
  - `description`: A brief description of the project. **Required**
  - `author`: The author's email address. **Required**
  - `output_path`: The path where the generated project will be created. **Required**
  - `type`: The type of project. Currently supported: `api`. **Required**

> **Note**: The `inference_type` is now automatically determined based on whether the `model` section is present (`online` if present, `offline` otherwise).

### 2. Model Configuration (**OPTIONAL**)
This section specifies details about the machine learning model. If this section is present, the project will be configured for online inference with MLflow model loading.

- **model**: Contains information about the MLflow model.
  - `author`: The model author's email (used for MLflow authentication). **Required**
  - `flavor`: The MLflow model flavor. Supported: `sklearn`, `xgboost`, `lightgbm`, `catboost`, `pytorch`, `tensorflow`, `keras`, `onnx`, `pyfunc`. **Required**
  - `path`: The MLflow model URI (e.g., `s3://bucket/path/to/model` or `mlflow-artifacts:/...`). **Required**

### 3. Serve Endpoint Schemas (**OPTIONAL**)
Defines the REST API endpoints that your service will expose.

- **serve_endpoint_schemas**: Contains a list of endpoint definitions.
  - `endpoints`: List of endpoint configurations.
    - `endpoint_name`: Name/path of the endpoint (e.g., `recommend`, `predict`). **Required**
    - `method`: HTTP method (`GET` or `POST`). **Required**
    - `request_schema`: Request parameter definitions.
      - `path_params`: List of path parameters (for URLs like `/users/{id}`).
      - `query_params`: List of query parameters.
      - `headers`: List of header parameters.
      - `body`: List of request body fields (for POST requests).
    - `response_schema`: Response definition.
      - `body`: List of response body fields.

**Field Definition Format** (for path_params, query_params, headers, body):
```yaml
- name: "field_name"
  type: "str"  # str, int, float, bool, list, dict
  description: "Field description"
  required: true  # or false
```

### 4. Runtime Environment (**OPTIONAL**)
Specifies additional environment variables and dependencies required for the service.

- **runtime_environment**: Lists environment variables and dependencies.
  - `env_variables`: List of environment variable key-value pairs.
  - `dependencies`: List of additional Python packages to install.

### 5. Feature Store Integration (**OPTIONAL**)
Defines the feature groups to be fetched from the feature store.

- **feature_store**: Lists feature groups and their configurations.
  - `feature_groups`: List of feature group configurations.
    - `name`: Name of the feature group. **Required**
    - `version`: Version of the feature group (e.g., `v1`). Optional, defaults to `V1`.
    - `primary_keys`: List of primary key column names. **Required**
    - `features`: List of feature column names to fetch. **Required**

### 6. API Client (**OPTIONAL**)
Specifies external API endpoints that the service will interact with.

- **api_client**: Lists external API endpoint configurations.
  - `endpoints`: List of external API definitions.
    - `endpoint_name`: Identifier for the API client. **Required**
    - `url`: Full URL of the external API. **Required**
    - `path`: API path (used for documentation). **Required**
    - `method`: HTTP method (`GET` or `POST`). **Required**
    - `request_schema`: Request parameter definitions.
      - `path_params`: List of path parameters.
      - `query_params`: List of query parameters.
      - `headers`: List of header parameters.
      - `body`: List of request body fields (for POST requests).
    - `response_schema`: Expected response definition.
      - `body`: List of response body fields.

---

## Sample YAML Configuration

Below is a complete sample YAML configuration demonstrating all available options:

```yaml
# =============================================================================
# REPOSITORY CONFIGURATION (REQUIRED)
# =============================================================================
repository:
  name: "ml-serve-testing"                                    # Required
  description: "User recommendation service for personalized suggestions"  # Required
  author: "user@serve.com"                                    # Required
  output_path: "/path/to/output"                              # Required
  type: "api"                                                 # Required (currently only "api" is supported)

# =============================================================================
# MODEL CONFIGURATION (OPTIONAL - include for online inference)
# =============================================================================
model:
  author: "user@serve.com"                                    # Required
  flavor: "sklearn"                                           # Required: sklearn, xgboost, pytorch, etc.
  path: "s3://my-bucket/models/recommendation_model"          # Required: MLflow model URI

# =============================================================================
# SERVE ENDPOINT SCHEMAS (OPTIONAL)
# =============================================================================
serve_endpoint_schemas:
  endpoints:
    # POST endpoint example
    - endpoint_name: "recommend"
      method: "POST"
      request_schema:
        path_params: []
        query_params: []
        headers: []
        body:
          - name: "user_id"
            type: "int"
            description: "Unique identifier for the user"
            required: true
          - name: "product_id"
            type: "int"
            description: "Unique identifier for the product"
            required: true
          - name: "user_age"
            type: "int"
            description: "Age of the user"
            required: false
          - name: "user_purchase_history"
            type: "list"
            description: "List of past purchases made by the user"
            required: false
      response_schema:
        body:
          - name: "recommendation_score"
            type: "float"
            description: "Score indicating user-product affinity"
            required: true

    # GET endpoint example
    - endpoint_name: "health"
      method: "GET"
      request_schema:
        path_params: []
        query_params:
          - name: "verbose"
            type: "bool"
            description: "Return detailed health info"
            required: false
        headers: []
        body: []
      response_schema:
        body:
          - name: "status"
            type: "str"
            description: "Health status"
            required: true

# =============================================================================
# RUNTIME ENVIRONMENT (OPTIONAL)
# =============================================================================
runtime_environment:
  env_variables:
    - ENV: "production"
    - API_KEY: "your-api-key-here"
  dependencies:
    - "xgboost==2.0.3"
    - "pandas==2.2.1"
    - "numpy==1.26.4"

# =============================================================================
# FEATURE STORE INTEGRATION (OPTIONAL)
# =============================================================================
feature_store:
  feature_groups:
    - name: "ds_bundle_promo_metadata_fg"
      version: "v1"
      primary_keys: ["id"]
      features:
        - "promotions"
        - "timestamp"
    - name: "asset_rank_fg"
      version: "v1"
      primary_keys: ["module", "userid", "variant"]
      features:
        - "assets_ranked"
        - "timestamp"

# =============================================================================
# API CLIENT (OPTIONAL)
# =============================================================================
api_client:
  endpoints:
    # GET API example
    - endpoint_name: "user_profile_service"
      url: "http://user-profile-service/api/v1/profiles"
      path: "/api/v1/profiles"
      method: "GET"
      request_schema:
        path_params: []
        query_params:
          - name: "user_id"
            type: "str"
            required: true
            description: "Unique identifier for the user"
          - name: "city"
            type: "str"
            required: false
            description: "City of the user"
        headers:
          - name: "page"
            type: "str"
            required: false
            description: "Page number"
          - name: "is_active"
            type: "bool"
            required: false
            description: "Is the user active"
        body: []
      response_schema:
        body:
          - name: "profile_data"
            type: "dict"
            description: "User profile information"
            required: true

    # POST API example
    - endpoint_name: "add_user"
      url: "http://user-profile-service/api/v1/profiles"
      path: "/api/v1/profiles"
      method: "POST"
      request_schema:
        path_params: []
        query_params:
          - name: "city"
            type: "str"
            required: false
            description: "City of the user"
        headers:
          - name: "page"
            type: "str"
            required: false
            description: "Page number"
          - name: "is_active"
            type: "bool"
            required: false
            description: "Is the user active"
        body:
          - name: "user_id"
            type: "str"
            required: true
            description: "Unique identifier for the user"
          - name: "devices"
            type: "list"
            required: true
            description: "List of devices"
          - name: "metadata"
            type: "dict"
            required: true
            description: "Extra metadata"
      response_schema:
        body:
          - name: "user_id"
            type: "str"
            description: "Created user ID"
            required: true
          - name: "status"
            type: "str"
            description: "Creation status"
            required: true
```

---

## Supported Data Types

| Type | Description | Example |
|------|-------------|---------|
| `str` | String value | `"hello"` |
| `int` | Integer value | `42` |
| `float` | Floating point number | `3.14` |
| `bool` | Boolean value | `true` / `false` |
| `list` | List/Array of values | `[1, 2, 3]` |
| `dict` | Dictionary/Object | `{"key": "value"}` |

---

## Usage

Generate a project from the YAML configuration:

```bash
hermes create-serve-repo -f /path/to/config.yaml
```

Or run interactively without a YAML file:

```bash
hermes create-serve-repo
```
