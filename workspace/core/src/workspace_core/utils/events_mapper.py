from datetime import datetime

from workspace_core.constants.constants import DATE_FORMAT
from workspace_core.dto.response import CodespaceResponse, ProjectResponse


def chronos_events_mapper(codespace: CodespaceResponse, project: ProjectResponse, data: dict):
    if codespace:
        data["codespace"] = {
            "id": str(codespace.id),
            "project_id": str(codespace.project_id),
            "name": codespace.name,
            "created_at": codespace.created_at.strftime(DATE_FORMAT),
            "updated_at": codespace.updated_at.strftime(DATE_FORMAT),
            "last_synced_at": codespace.last_synced_at.strftime(DATE_FORMAT),
        }
    else:
        data["codespace"] = None

    if project:
        data["project"] = {
            "id": str(project.id),
            "user_id": project.user_id,
            "name": project.name,
            "created_at": project.created_at,
            "updated_at": project.updated_at,
        }
    else:
        data["project"] = None
    return data
