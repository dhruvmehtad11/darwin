import asyncio
import requests
import aiohttp
from typing import Dict, Any, Optional
from requests.exceptions import RequestException


def call_api_endpoint(
    url: str,
    method: str = "GET",
    headers: Optional[Dict[str, str]] = None,
    params: Optional[Dict[str, Any]] = None,
    json_data: Optional[Dict[str, Any]] = None,
    timeout: int = 30,
) -> Dict[str, Any]:
    """
    Makes an HTTP request to an API endpoint and returns the JSON response.

    Args:
        url (str): The API endpoint URL
        method (str): HTTP method (GET, POST, PUT, DELETE, etc.). Defaults to "GET"
        headers (Dict[str, str], optional): Request headers
        params (Dict[str, Any], optional): URL parameters
        json_data (Dict[str, Any], optional): JSON payload for POST/PUT requests
        timeout (int): Request timeout in seconds. Defaults to 30

    Returns:
        Dict[str, Any]: JSON response from the API

    Raises:
        RequestException: If the API request fails
        ValueError: If the response is not valid JSON
    """
    try:
        response = requests.request(
            method=method.upper(),
            url=url,
            headers=headers,
            params=params,
            json=json_data,
            timeout=timeout,
        )

        # Raise an exception for bad status codes
        response.raise_for_status()

        # Return JSON response
        return response.json()

    except RequestException as e:
        raise RequestException(f"API request failed: {str(e)}")
    except ValueError as e:
        raise ValueError(f"Failed to parse JSON response: {str(e)}")


async def call_api_endpoint_async(
    url: str,
    method: str = "GET",
    headers: Optional[Dict[str, str]] = None,
    params: Optional[Dict[str, Any]] = None,
    json_data: Optional[Dict[str, Any]] = None,
    timeout: int = 30,
) -> Dict[str, Any]:
    """
    Makes an asynchronous HTTP request to an API endpoint and returns the JSON response.

    Args:
        url (str): The API endpoint URL
        method (str): HTTP method (GET, POST, PUT, DELETE, etc.). Defaults to "GET"
        headers (Dict[str, str], optional): Request headers
        params (Dict[str, Any], optional): URL parameters
        json_data (Dict[str, Any], optional): JSON payload for POST/PUT requests
        timeout (int): Request timeout in seconds. Defaults to 30

    Returns:
        Dict[str, Any]: JSON response from the API

    Raises:
        ClientError: If the API request fails
        ValueError: If the response is not valid JSON
    """
    try:
        async with aiohttp.ClientSession(headers=headers) as session:
            async with session.request(
                method=method.upper(),
                url=url,
                params=params,
                json=json_data,
                timeout=timeout,
            ) as response:
                if response.status >= 400:
                    error_body = await response.text()
                    raise aiohttp.ClientResponseError(
                        response.request_info,
                        response.history,
                        status=response.status,
                        message=error_body,
                    )
                return await response.json()
    except asyncio.TimeoutError as e:
        raise asyncio.TimeoutError(f"API request timed out after {timeout} seconds: {str(e)}")
    except aiohttp.ClientResponseError as e:
        raise Exception(f"Client response error {e.message}")
    except aiohttp.ClientError as e:
        raise aiohttp.ClientError(f"API request failed: {str(e)}")
    except ValueError as e:
        raise ValueError(f"Failed to parse JSON response: {str(e)}")
