import pytest
import os
import sys
from typing import Generator
import asyncio
from hermes.src.template_service.constants import required_fields
import subprocess
from pathlib import Path

# Add the src directory to the Python path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))


# Configure pytest-asyncio
def pytest_configure(config):
    config.addinivalue_line("markers", "asyncio: mark test as async")
    config.addinivalue_line("markers", "api: mark test as API integration test")


@pytest.fixture(scope="session")
def event_loop() -> Generator[asyncio.AbstractEventLoop, None, None]:
    """Create an instance of the default event loop for the test session."""
    import asyncio

    loop = asyncio.get_event_loop_policy().new_event_loop()
    yield loop
    loop.close()


@pytest.fixture
def sample_yaml_config():
    """Provide a sample YAML configuration for testing."""
    return {
        "repository_name": "test-repo",
        "repository_author": "test@example.com",
        "repository_type": "fastapi",
        "model_author": "test@example.com",
        "model_flavor": "sklearn",
        "model_path": "models:/test-model/production",
        "repository_inference_type": "online",
    }


@pytest.fixture
def required_fields_fixture():
    """Provide the list of required fields for testing."""
    return required_fields


def pip_install_requirements():
    """Install packages from requirements.txt"""
    requirements_path = Path(__file__).parent.parent / "requirements.txt"

    if not requirements_path.exists():
        pytest.fail("requirements.txt not found in project root")

    try:
        subprocess.check_call([sys.executable, "-m", "pip", "install", "-r", str(requirements_path)])
    except subprocess.CalledProcessError as e:
        pytest.fail(f"Failed to install requirements: {e}")


@pytest.fixture(scope="session", autouse=True)
def setup_requirements():
    """Fixture to verify and install requirements before any tests run"""
    pip_install_requirements()
