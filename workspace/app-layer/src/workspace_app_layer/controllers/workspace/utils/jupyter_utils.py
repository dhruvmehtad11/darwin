from workspace_core.constants.constants import CODESPACE_URL
from workspace_core.constants.config import Config


def get_jupyter_suffix(env: str, user_id: str, project_name: str, codespace_name: str) -> str:
    return f"{CODESPACE_URL}{Config(env).fsx_root}{user_id}/{project_name}/{codespace_name}"


def get_code_server_link(env: str, user_id: str, project_name: str, codespace_name: str) -> str:
    return f"folder=/home/ray/{Config(env).fsx_root}{user_id}/{project_name}/{codespace_name}"
