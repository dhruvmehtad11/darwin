from fastapi.responses import Response

from src.client.mysql_client import MysqlClient
from src.constant.queries import HEALTHCHECK_QUERY
from src.dto.BaseResponse import BaseResponse
from loguru import logger

async def health_check(mysql_client: MysqlClient) -> Response:
    master_health = None
    slave_health = None
    master_health_error = None
    slave_health_error = None
    try:
        master_health = await mysql_client.execute_query(HEALTHCHECK_QUERY, raw_res=True)
        if type(master_health) == tuple:
            master_health = master_health[0]
            if type(master_health) == tuple:
                master_health = master_health[0]
        if master_health != 1:
            raise Exception(master_health)
    except Exception as e:
        logger.exception(f"Mysql master healthcheck failed: {e}")
        master_health_error = str(e)

    try:
        slave_health = await mysql_client.execute_query(HEALTHCHECK_QUERY, is_select_query=True, raw_res=True)
        if type(slave_health) == tuple:
            slave_health = slave_health[0]
            if type(slave_health) == tuple:
                slave_health = slave_health[0]
        if slave_health != 1:
            logger.exception(f"Mysql slave healthcheck failed with slave_health: {slave_health}")
            raise Exception(slave_health)
    except Exception as e:
        logger.exception(f"Mysql slave healthcheck failed: {e}")
        slave_health_error = str(e)

    error: bool = master_health_error is not None or slave_health_error is not None
    body = {
        "status": "UP" if not error else "DOWN",
        "mysql": {
            "master": master_health if master_health_error is None else master_health_error,
            "slave": slave_health if slave_health_error is None else slave_health_error
        }
    }
    code = 200 if not error else 500
    return BaseResponse(body).get_json_response(code)
