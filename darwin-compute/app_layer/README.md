# App Layer

FastAPI app layer for Darwin Compute.

This folder is responsible for taking requests from the UI, converting it to `compute_models`, and interacting with the `compute_core` to perform the required actions.

## Folder Structure

    .
    ├── src
    │   └── compute_app_layer
    │       ├── config              # Configuration files
    │       │   └── constants.py    # Constants used in the app layer
    │       ├── controllers         # Controllers for the app layer. These are responsible for handling the requests.
    │       ├── main.py             # Main FastAPI app
    │       ├── models              # Pydantic models for the app layer. Contract between the UI and the app layer.
    │       ├── routers             # Routers for the app layer. These are responsible for routing the requests to the controllers.
    │       └── utils               # Utility functions for the app layer
    └── tests                       # Tests for the app layer


## Components

1. Remote Command Layer