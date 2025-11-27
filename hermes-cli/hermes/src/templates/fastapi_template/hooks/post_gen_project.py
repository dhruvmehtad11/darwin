import os
import json
import shutil
from pathlib import Path
import subprocess


def copy_model_files():
    """
    Copy model files from the path specified in cookiecutter.json to the generated
    project's resources/saved_models folder.
    Creates the target directory if it doesn't exist.
    """
    # Read cookiecutter.json to get the model path
    cookiecutter_json_path = Path("../cookiecutter.json")
    try:
        with open(cookiecutter_json_path) as f:
            config = json.load(f)
            source_dir = Path(config.get("model_path", ""))

        if not source_dir or not source_dir.exists():
            print(f"Invalid or missing model path: {source_dir}")
            return

        # Target directory in the generated project
        target_dir = Path("resources/saved_models")
        target_dir.mkdir(parents=True, exist_ok=True)

        # Copy all files from source to target directory
        for item in source_dir.glob("*"):
            if item.is_file():
                shutil.copy2(item, target_dir)
                print(f"Copied model file: {item.name}")

    except Exception as e:
        print(f"Error during model file copying: {str(e)}")


def delete_unused_serve_folders():
    """
    Delete unused server folders based on server_type selection from cookiecutter config.
    """
    REMOVE_PATHS = [
        '{% if cookiecutter.repository_type == "fastapi" %}./src/{{ cookiecutter.repository_name }}/workflow_serve{% endif %}',
        '{% if cookiecutter.repository_type == "workflow" %}./src/{{ cookiecutter.repository_name }}/app_layer{% endif %}',
    ]
    for path in REMOVE_PATHS:
        path = path.strip()
        if path and os.path.exists(path):
            if os.path.isfile(path):
                os.unlink(path)
            else:
                shutil.rmtree(path)


def setup_github_repo():
    """
    Check if GitHub CLI is installed, install it if not, log in to GitHub,
    create a repository, and push the current directory to the new repo.
    Then trigger the ephemeral runner onboarding workflow.
    """
    # Prompt user for GitHub repository setup
    user_input = input("Do you want to upload the repository to GitHub? (yes/no) [no]: ").strip().lower() or "no"
    if user_input != "yes":
        print("Skipping GitHub repository setup.")
        return

    # Check if GitHub CLI is installed
    try:
        subprocess.run(
            ["gh", "--version"],
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )
    except subprocess.CalledProcessError:
        # Install GitHub CLI using brew if not installed
        print("GitHub CLI not found. Installing using brew...")
        subprocess.run(["brew", "install", "gh"], check=True)

    # Log in to GitHub
    print("Logging in to GitHub...")
    subprocess.run(["gh", "auth", "login", "-s", "workflow"], check=True)

    repository_name = "{{ cookiecutter.repository_name }}"

    # Initialize git and push to GitHub
    print("Initializing git repository and pushing to GitHub...")
    subprocess.run(["git", "init"], check=True)
    subprocess.run(["git", "add", "."], check=True)
    subprocess.run(["git", "commit", "-m", "Initial commit"], check=True)
    subprocess.run(["git", "branch", "-M", "main"], check=True)
    username = subprocess.run(
        ["gh", "api", "user", "--jq", ".login"], capture_output=True, text=True, check=True
    ).stdout.strip()
    # Create a new GitHub repository
    print(f"Creating GitHub repository: {repository_name}...")
    subprocess.run(
        [
            "gh",
            "repo",
            "create",
            repository_name,
            "--private",
            "--source=.",
            "--remote=origin",
        ],
        check=True,
    )
    subprocess.run(["git", "push", "-u", "origin", "main"], check=True)

    # Trigger the ephemeral runner onboarding workflow
    print("Triggering ephemeral runner onboarding workflow...")
    try:
        subprocess.run(
            [
                "gh",
                "workflow",
                "run",
                "ephemeral-runner-repo-onboarding.yaml",
                "--repo",
                f"{username}/{repository_name}",
            ],
            check=True,
        )
        print("Successfully triggered ephemeral runner onboarding workflow")
    except subprocess.CalledProcessError as e:
        print(f"Warning: Failed to trigger ephemeral runner workflow: {str(e)}")


def remove_directories_based_on_inference_type():
    """
    Remove directories and files based on the repository_inference_type from cookiecutter.json.
    """
    # Read cookiecutter.json to get the inference type

    inference_type = "{{ cookiecutter.repository_inference_type }}"

    directories_to_remove = []
    # Define directories and files to remove based on inference type
    if inference_type == "online":
        directories_to_remove = []
    elif inference_type == "offline":
        directories_to_remove = [
            "src/model",
            "src/feature_store",
            "src/app_layer/dao/feature_store_dao.py",
        ]

    try:
        # Remove the specified directories and files
        for path in directories_to_remove:
            path_obj = Path(path)
            if path_obj.exists():
                if path_obj.is_dir():
                    shutil.rmtree(path_obj)
                else:
                    path_obj.unlink()

    except Exception as e:
        print(f"Error during directory/file removal: {str(e)}")


def setup_virtualenv_and_install_tools():
    """
    Check if pylint and black are installed. If not, create a virtual environment,
    activate it, and install pylint and black.
    """
    venv_path = Path(".venv")
    if not venv_path.exists():
        print("Creating virtual environment...")
        subprocess.run(
            ["python3", "-m", "venv", str(venv_path)],
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )

    # Activate the virtual environment
    activate_script = venv_path / "bin" / "activate"
    if not activate_script.exists():
        print("Error: Virtual environment activation script not found.")
        return False

    # Check if pylint and black are installed
    try:
        subprocess.run(
            ["black", "--version"],
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )
        subprocess.run(
            ["pylint", "--version"],
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )
    except subprocess.CalledProcessError:
        print("Installing pylint and black in the virtual environment...")
        subprocess.run(
            [str(venv_path / "bin" / "pip"), "install", "pylint", "black"],
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )

    return True


def lint_and_format_code():
    """
    Lint the code using pylint and format it using black.
    """

    # Run black for code formatting
    print("Running black for code formatting...")
    subprocess.run(
        ["black", "src"],
        check=False,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )

    # Run pylint for code linting
    print("Running pylint for code linting...")
    subprocess.run(
        ["pylint", "src"],
        check=False,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )


def rename_template_files():
    for root, _, files in os.walk("."):
        for f in files:
            if f.endswith(".py.jinja"):
                src = os.path.join(root, f)
                dst = os.path.join(root, f[:-6])  # Strip .jinja
                shutil.move(src, dst)


if __name__ == "__main__":
    if setup_virtualenv_and_install_tools():
        rename_template_files()
        remove_directories_based_on_inference_type()
        delete_unused_serve_folders()
        lint_and_format_code()
        setup_github_repo()
