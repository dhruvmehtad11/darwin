# Core

Core is a folder that contains the core functionality of the project. It is the heart of the project and contains the main logic.

It is responsible for interacting with the databases, which include MySQL and Elasticsearch.
It also interacts with the underlying services like the DCM (Darwin Cluster Manager) and Chronos (Event Service).

## Folder Structure
    
    .
    ├── src
    │   └── compute_core
    │       ├── compute.py              # Main compute logic
    │       ├── constant                # Configuration files
    │       │   ├── config.py           # Configuration file for the core layer
    │       │   ├── constants.py        # Constants used in the core layer
    │       │   └── logging.conf        # Logging configuration
    │       ├── dao                     # Data Access Objects for interacting with the databases
    │       │   ├── es_dao.py           # Generic Elasticsearch DAO
    │       │   ├── mysql_dao.py        # Generic MySQL DAO
    │       │   └── queries             # SQL and ES Queries
    │       ├── dto                     # Data Transfer Objects. Models specific for the core layer.
    │       ├── jupyter.py              # Jupyter notebook related logic
    │       ├── service                 # Services for interacting with the underlying services
    │       └── util                    # Utility functions for the core layer
    │           ├── resources       
    │           │   └── values.yaml     # Demo Values file sent to DCM
    │           └── yaml_generator_v2   # Logic to generate the values.yaml file for Ray clusters.
    └── tests                           # Tests for the core layer
