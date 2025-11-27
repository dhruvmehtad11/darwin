import requests

from workspace_app_layer.models.workspace.cheque_unique_request import (
    CheckUniqueProjectNameRequest,
    CheckUniqueCodespaceNameRequest,
    CheckUniqueGithubLinkRequest,
)
from workspace_app_layer.utils.utils import error_handler
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


def check_unique_project_name(workspace, request: CheckUniqueProjectNameRequest):
    try:
        formatted_project_name = request.project_name.replace(" ", "-")
        resp = workspace.check_unique_project_name(request.user, formatted_project_name)
        logger.debug(f"check_unique_project_name {request.project_name}: {resp}")
    except Exception as err:
        logger.error(f"Error in check_unique_project_name {request.project_name}: {err}")
        return error_handler(err.__str__())

    check_unique_project_name_response = {"status": "SUCCESS", "data": {"is_unique": resp}}
    return check_unique_project_name_response


def get_git_project_details(github_link: str):
    temp = github_link.split("/")
    project_name = temp[len(temp) - 1].split(".git")[0]
    user_name = temp[len(temp) - 2]
    return {"project": project_name, "user": user_name}


def check_unique_github_link(workspace, request: CheckUniqueGithubLinkRequest):
    project_info = get_git_project_details(request.github_link)

    api_url = f"https://api.github.com/repos/{project_info['user']}/{project_info['project']}"
    headers = {"Accept": "application/vnd.github+json"}

    resp = requests.request("GET", url=api_url, headers=headers)
    if resp.status_code > 200:
        logger.error(f"Error in check_unique_github_link: {resp.text}")
        return error_handler("Invalid Github URL")
    try:
        resp = workspace.check_unique_github_link(request.user, request.github_link)
        logger.debug(f"check_unique_github_link {request.github_link}: {resp}")
    except Exception as err:
        logger.error(f"Error in check_unique_github_link {request.github_link}: {err}")
        return error_handler(err.__str__())

    check_unique_github_link_response = {"status": "SUCCESS", "data": {"is_unique": resp}}
    return check_unique_github_link_response


def check_unique_codespace_name(workspace, request: CheckUniqueCodespaceNameRequest):
    try:
        formatted_cs_name = request.codespace_name.replace(" ", "-")
        resp = workspace.check_unique_codespace_name(request.project_id, formatted_cs_name)
        logger.debug(f"check_unique_codespace_name {request.codespace_name}: {resp}")
    except Exception as err:
        logger.error(f"Error in check_unique_codespace_name {request.codespace_name}: {err}")
        return error_handler(err.__str__())

    check_unique_codespace_name_response = {"status": "SUCCESS", "data": {"is_unique": resp}}
    return check_unique_codespace_name_response
