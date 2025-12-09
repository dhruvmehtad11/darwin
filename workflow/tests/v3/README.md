# V3 Workflow Test Suite

This directory contains comprehensive tests for the V3 workflow system, focusing on the newly written V3 modules.

## Test Structure

```
tests/v3/
├── __init__.py
├── conftest.py                 # Pytest fixtures and configuration
├── test_integration.py         # Integration tests
├── run_tests.py               # Test runner script
└── README.md                  # This file

model/tests/v3/
├── __init__.py
└── test_task_models.py        # Model layer tests

core/tests/v3/
├── __init__.py
└── test_dag_creator_service.py # Core layer tests

app_layer/tests/v3/
├── __init__.py
└── test_workflow_api.py       # API layer tests

airflow/tests/v3/
├── __init__.py
└── test_pelican_operator.py   # Airflow integration tests
```

## Test Coverage

### 1. Model Layer Tests (`model/tests/v3/`)
- **PelicanArtifact**: Tests for artifact model with `args` field
- **PelicanConfig**: Tests for Pelican configuration model
- **DarwinConfig**: Tests for Darwin configuration model
- **TaskV3**: Tests for task model with validation
- **Validation**: Tests for field validation and error handling

### 2. Core Layer Tests (`core/tests/v3/`)
- **DagCreator**: Tests for DAG creation service
- **Task Args Building**: Tests for building task arguments
- **Dependencies**: Tests for task dependency handling
- **JSON Serialization**: Tests for data serialization

### 3. API Layer Tests (`app_layer/tests/v3/`)
- **Workflow API**: Tests for V3 API endpoints
- **Request Validation**: Tests for request validation
- **Error Handling**: Tests for error scenarios
- **Args Field**: Tests for `args` field in API requests

### 4. Airflow Integration Tests (`airflow/tests/v3/`)
- **PelicanOperator**: Tests for Pelican operator
- **Payload Building**: Tests for payload construction with `args`
- **Execution Flow**: Tests for operator execution
- **Error Scenarios**: Tests for failure handling

### 5. Integration Tests (`tests/v3/`)
- **End-to-End**: Complete workflow creation flow
- **Mixed Workflows**: Tests with both Pelican and Darwin tasks
- **Dependencies**: Tests for task dependencies
- **Args Handling**: Comprehensive tests for `args` field

## Key Features Tested

### Args Field Support
- ✅ Valid args list (strings, numbers, booleans as strings)
- ✅ Empty args list
- ✅ Missing args field (defaults to empty list)
- ✅ Complex args with mixed types
- ✅ JSON serialization of args
- ✅ Args in PelicanOperator payload

### Task Configuration
- ✅ Pelican task configuration with all fields
- ✅ Darwin task configuration
- ✅ Task dependencies
- ✅ Task validation
- ✅ Default values

### API Endpoints
- ✅ Create workflow
- ✅ Get workflow by ID/name
- ✅ Update workflow
- ✅ Delete workflow
- ✅ Search workflows
- ✅ Error handling

### DAG Creation
- ✅ Complete DAG creation flow
- ✅ Task definition building
- ✅ Dependency resolution
- ✅ JSON serialization

## Running Tests

### Prerequisites
```bash
# Install test dependencies
pip install pytest pytest-cov pytest-mock

# Set up test environment
export TESTING=true
export ENVIRONMENT=test
export LOG_LEVEL=DEBUG
```

### Run All Tests
```bash
# From project root
python tests/v3/run_tests.py
```

### Run Specific Test Categories
```bash
# Unit tests only
python tests/v3/run_tests.py --unit-only

# API tests only
python tests/v3/run_tests.py --api-only

# Airflow tests only
python tests/v3/run_tests.py --airflow-only

# Integration tests only
python tests/v3/run_tests.py --integration-only
```

### Run with Coverage
```bash
# Run all tests with coverage report
python tests/v3/run_tests.py --coverage
```

### Run Specific Test
```bash
# Run specific test file
python tests/v3/run_tests.py --test model/tests/v3/test_task_models.py

# Run specific test function
python tests/v3/run_tests.py --test "model/tests/v3/test_task_models.py::TestPelicanArtifact::test_valid_pelican_artifact"
```

### Direct pytest Commands
```bash
# Run all V3 tests
pytest model/tests/v3/ core/tests/v3/ app_layer/tests/v3/ airflow/tests/v3/ tests/v3/ -v

# Run with coverage
pytest model/tests/v3/ core/tests/v3/ app_layer/tests/v3/ airflow/tests/v3/ tests/v3/ --cov=workflow_model.v3 --cov=workflow_core.v3 --cov=workflow_app_layer.v3 --cov-report=html

# Run specific test file
pytest model/tests/v3/test_task_models.py -v

# Run tests matching pattern
pytest -k "args" -v
```

## Test Fixtures

The `conftest.py` file provides reusable fixtures:

- `sample_pelican_artifact`: Sample PelicanArtifact with args
- `sample_pelican_config`: Sample PelicanConfig
- `sample_darwin_config`: Sample DarwinConfig
- `sample_pelican_task`: Sample Pelican TaskV3
- `sample_darwin_task`: Sample Darwin TaskV3
- `sample_workflow_request`: Sample CreateWorkflowRequestV3
- `mock_workflow_service`: Mock workflow service
- `mock_pelican_operator`: Mock PelicanOperator
- `complex_args_list`: Complex args for testing
- `empty_args_list`: Empty args for testing

## Test Data

### Args Field Test Cases
```python
# Valid args
args = ["10", "20", "30"]
args = ["hello", "world"]
args = ["true", "false", "3.14"]
args = []  # Empty list
# No args field (defaults to [])

# Invalid args (should fail validation)
args = "not_a_list"  # Wrong type
args = [1, 2, 3]     # Numbers instead of strings
```

### Task Configuration Test Cases
```python
# Pelican task with full config
task = TaskV3(
    task_name="test_task",
    task_type="pelican",
    task_config=PelicanConfig(
        artifact=PelicanArtifact(
            file="s3://bucket/test.jar",
            class_name="org.example.TestClass",
            spark_version="3.5.0",
            args=["10", "20"]
        ),
        cluster={"engineType": "SPARK"},
        spark_configs={"spark.executor.memory": "4g"},
        tags={"env": "test"}
    )
)

# Darwin task with dependencies
task = TaskV3(
    task_name="darwin_task",
    task_type="darwin",
    task_config=DarwinConfig(
        source="workspace://test",
        cluster_type="basic"
    ),
    depends_on=["pelican_task"]
)
```

## Best Practices

### Writing Tests
1. **Use descriptive test names**: `test_pelican_artifact_with_args`
2. **Follow AAA pattern**: Arrange, Act, Assert
3. **Test edge cases**: Empty args, missing fields, invalid data
4. **Use fixtures**: Reuse common test data
5. **Mock external dependencies**: Don't make real API calls

### Test Organization
1. **Group related tests**: Use test classes for related functionality
2. **Test one thing at a time**: Each test should verify one behavior
3. **Use setup/teardown**: Clean up test state
4. **Document test purpose**: Use docstrings to explain test intent

### Error Testing
1. **Test validation errors**: Invalid data should raise ValidationError
2. **Test service errors**: Mock service failures
3. **Test API errors**: HTTP error responses
4. **Test edge cases**: Boundary conditions and limits

## Continuous Integration

### GitHub Actions Example
```yaml
name: V3 Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up Python
        uses: actions/setup-python@v2
        with:
          python-version: 3.9
      - name: Install dependencies
        run: |
          pip install -r requirements.txt
          pip install pytest pytest-cov pytest-mock
      - name: Run V3 tests
        run: python tests/v3/run_tests.py --coverage
      - name: Upload coverage
        uses: codecov/codecov-action@v1
```

## Troubleshooting

### Common Issues
1. **Import errors**: Ensure PYTHONPATH includes project root
2. **Missing dependencies**: Install pytest and related packages
3. **Environment variables**: Set TESTING=true and other required vars
4. **Mock issues**: Ensure mocks are properly configured

### Debug Mode
```bash
# Run with verbose output
pytest -v -s --tb=long

# Run single test with debugger
pytest -s --pdb model/tests/v3/test_task_models.py::TestPelicanArtifact::test_valid_pelican_artifact
```

## Coverage Goals

- **Model Layer**: 95%+ coverage
- **Core Layer**: 90%+ coverage  
- **API Layer**: 85%+ coverage
- **Airflow Integration**: 80%+ coverage
- **Integration Tests**: 100% of critical paths

## Contributing

When adding new V3 functionality:

1. **Write tests first**: Follow TDD approach
2. **Update fixtures**: Add new fixtures to conftest.py
3. **Test edge cases**: Include error scenarios
4. **Update documentation**: Keep this README current
5. **Run full suite**: Ensure all tests pass

## Notes

- Tests focus on V3 modules only, ignoring legacy V2 code
- Args field is a key feature being tested throughout
- Integration tests verify end-to-end functionality
- Mocking is used extensively to avoid external dependencies 