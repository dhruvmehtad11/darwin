import aiohttp
import requests

from loguru import logger
from typing import Optional


def make_api_request(
    method: str,
    url: str,
    headers: Optional[dict] = None,
    data: Optional[dict] = None,
    timeout: int = 5,
    max_retries: int = 1,
):
    retries = 0
    while retries < max_retries:
        try:
            response = requests.request(method, url, headers=headers, json=data, timeout=timeout)
            if not 200 <= response.status_code < 300:
                logger.error(f"Error occurred in API {method} - {url} - {response.text}, body - {data}")
                response.raise_for_status()
            return response.json()
        except Exception as e:
            logger.error(f"Error occurred in API {method} - {url} - {e}")
            retries += 1
            if retries >= max_retries:
                raise e


async def make_async_api_request(
    method: str,
    url: str,
    timeout: int = 5,
    max_retries: int = 1,
    headers: Optional[dict] = None,
    data: Optional[dict] = None,
    params: Optional[dict] = None,
):
    retries = 0
    async with aiohttp.ClientSession() as session:
        while retries < max_retries:
            try:
                async with session.request(
                    method, url, headers=headers, json=data, timeout=timeout, params=params
                ) as response:
                    if not 200 <= response.status < 300:
                        logger.error(f"Error occurred in API {method} - {url} - {response}, body - {data}")
                        response.raise_for_status()

                    return await response.json()

            except Exception as e:
                logger.error(f"Error occurred in make_async_api_request {method} - {url} - {e}")
                retries += 1
                if retries >= max_retries:
                    raise e
