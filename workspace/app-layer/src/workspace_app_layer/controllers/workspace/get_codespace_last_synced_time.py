from workspace_app_layer.utils.utils import error_handler, serialize_date
from workspace_core.dto.response import CodespaceResponse
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


async def get_codespace_last_synced_time(workspace, codespace_id: int):
    try:
        resp: CodespaceResponse = workspace.codespace_details(codespace_id)
        return {"status": "SUCCESS", "data": {"last_synced_time": serialize_date(resp.last_synced_at)}}
    except Exception as err:
        logger.error(f"Error in getting last synced time for codespace {codespace_id}")
        return error_handler(err.__str__())
