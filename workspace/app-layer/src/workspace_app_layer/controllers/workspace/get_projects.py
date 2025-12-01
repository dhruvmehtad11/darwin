from typing import Optional

from workspace_app_layer.utils.utils import error_handler, serialize_date
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


def get_all_projects(workspace, user_id: str, my_projects: bool, sort_by: str, query: Optional[str] = ""):
    try:
        playground = workspace.get_playground(user_id)
        logger.debug(f"Playground for user_id {user_id}: {playground}")
        resp = workspace.list_projects(user_id, query, my_projects, sort_by)
        logger.debug(f"Projects for user_id {user_id}: {resp}")
    except Exception as err:
        logger.error(f"Error in get_all_projects for user {user_id}: {err}")
        return error_handler(err.__str__())

    projects_response = {"status": "SUCCESS", "data": []}

    query_contains_playground = query in "Playground" and my_projects

    if playground:
        if query_contains_playground:
            projects_response["data"].append(
                {
                    "project_id": playground["id"],
                    "project_name": playground["name"],
                    "number_of_codespaces": playground["codespace_count"],
                    "last_updated": serialize_date(playground["updated_at"]),
                    "created_on": serialize_date(playground["created_at"]),
                    "created_by": playground["user_id"],
                }
            )
    for project in resp:
        projects_response["data"].append(
            {
                "project_id": project["id"],
                "project_name": project["name"],
                "number_of_codespaces": project["codespace_count"],
                "last_updated": serialize_date(project["updated_at"]),
                "created_on": serialize_date(project["created_at"]),
                "created_by": project["user_id"],
            }
        )
    logger.debug(f"Projects response for user_id {user_id}: {projects_response}")
    return projects_response


def get_projects_by_user(workspace, user_id: str, query: Optional[str] = ""):
    try:
        resp = workspace.list_projects(user_id, query)
        logger.debug(f"Projects for user_id {user_id}: {resp}")
    except Exception as err:
        logger.error(f"Error in get_projects_by_user for user {user_id}: {err}")
        return error_handler(err.__str__())

    projects_response = {"status": "SUCCESS", "data": []}
    for project in resp:
        projects_response["data"].append(
            {
                "project_id": project["id"],
                "project_name": project["name"],
                "default_codespace": project["default_codespace"],
                "last_updated": serialize_date(project["updated_at"]),
            }
        )
    return projects_response


def get_project_count_controller(workspace, user_id: str):
    try:
        resp = workspace.get_project_count(user_id)
        logger.debug(f"Projects count for user_id {user_id}: {resp}")
        return {
            "status": "SUCCESS",
            "data": {"my_projects": resp["my_projects"], "other_projects": resp["other_projects"]},
        }
    except Exception as err:
        logger.error(f"Error in get_project_count_controller for user {user_ids}: {err}")
        return error_handler(err.__str__())
