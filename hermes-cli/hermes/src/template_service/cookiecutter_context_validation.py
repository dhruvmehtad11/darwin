from typing import Optional
from cookiecutter.main import cookiecutter
import importlib.resources as pkg_resources
from pathlib import Path
import subprocess

from hermes.src.template_service.configuration_manager import ConfigurationManager
from .validate_user_yaml import flatten_dict


def get_github_username() -> str:
    """
    Fetch GitHub username from gh CLI.
    Returns 'your-github-username' as fallback if gh CLI is not available.
    """
    try:
        username = subprocess.run(
            ["gh", "api", "user", "--jq", ".login"], capture_output=True, text=True, check=True, timeout=5
        ).stdout.strip()
        return username if username else "your-github-username"
    except (subprocess.CalledProcessError, subprocess.TimeoutExpired, FileNotFoundError):
        # If gh CLI is not installed or not authenticated, use placeholder
        return "your-github-username"


def create_serve_repo_from_template(template: Optional[str], filename: str, output_path: Optional[str]):
    """
    Create a new project from a template using optional YAML configuration.
    If `template` is None, it loads the built-in package template.
    """

    config_manager = ConfigurationManager()
    print(f"Creating new project from template {template or '[internal package template]'}")
    config = config_manager.load_from_yaml(filename)

    if not config:
        print("Starting interactive configuration:")
        config = config_manager.collect_interactive_input()
        output_file = "config.yaml"
        config_manager.save_to_yaml(output_file)
        print(f"\nConfiguration saved to {output_file}")

    # Set inference type
    config["repository_inference_type"] = "online" if config.get("model") else "offline"

    flattened_context = flatten_dict(config)
    print(f"Flattened context {flattened_context}")

    # Fetch GitHub username from gh CLI
    github_username = get_github_username()
    flattened_context["github_username"] = github_username
    print(f"GitHub username: {github_username}")

    # Default output path
    if flattened_context.get("repository_output_path") is None:
        flattened_context["repository_output_path"] = "."

    # Path of the current file (template_generator.py)
    CURRENT_FILE = Path(__file__).resolve()

    # Go up to 'src/' and then into 'templates/fastapi_template/'
    TEMPLATE_DIR = CURRENT_FILE.parent.parent / "templates" / "fastapi_template"

    print("Resolved template dir:", TEMPLATE_DIR)

    print("Using template path:", template)

    # Build cookiecutter kwargs
    cookiecutter_kwargs = {
        "extra_context": flattened_context,
        "no_input": True,
        "output_dir": output_path or flattened_context["repository_output_path"],
    }

    print(f"Cookiecutter kwargs: {cookiecutter_kwargs}")
    cookiecutter(str(TEMPLATE_DIR), **cookiecutter_kwargs)
