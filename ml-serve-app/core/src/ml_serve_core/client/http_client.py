import aiohttp


class AsyncHttpClient:
    """
    A reusable asynchronous HTTP client using aiohttp.
    """

    def __init__(self):
        self._session = None

    async def __aenter__(self):
        # Create the session when entering the context
        self._session = aiohttp.ClientSession()
        return self

    async def __aexit__(self, exc_type, exc_value, traceback):
        # Ensure session is closed on exit, even if an exception occurs
        if self._session:
            await self._session.close()

    async def _handle_response(self, response):
        result = {
            'status': response.status,
            'headers': dict(response.headers),
        }
        try:
            result['body'] = await response.json()
        except aiohttp.ContentTypeError:
            result['body'] = await response.text()

        # Raise exceptions for specific status codes
        if response.status >= 400 and response.status < 500:
            raise Exception(f"Bad Request (400): {result['body']}")
        if response.status >= 500:
            raise Exception(f"Server Error (500): {result['body']}")

        return result

    async def get(self, url: str, params: dict = None, headers: dict = None):
        """
        Perform an HTTP GET request.
        :param url: The endpoint URL.
        :param params: Query parameters.
        :param headers: Request headers.
        :return: The response
        """
        try:
            async with self._session.get(url, params=params, headers=headers) as response:
                return await self._handle_response(response)
        except Exception as e:
            raise Exception(f"Error performing GET request to {url}: {e}")

    async def post(self, url, data=None, json=None, headers=None):
        """
        Perform an HTTP POST request.
        :param url: The endpoint URL.
        :param data: Form data.
        :param json: JSON data.
        :param headers: Request headers.
        :return: The response
        """
        try:
            async with self._session.post(url, data=data, json=json, headers=headers) as response:
                return await self._handle_response(response)
        except Exception as e:
            raise Exception(f"Error performing POST request to {url}: {e}")

    async def patch(self, url, data=None, json=None, headers=None):
        """
        Perform an HTTP PATCH request.
        :param url: The endpoint URL.
        :param data: Form data.
        :param json: JSON data.
        :param headers: Request headers.
        :return: The response
        """
        try:
            async with self._session.patch(url, data=data, json=json, headers=headers) as response:
                return await self._handle_response(response)
        except Exception as e:
            raise Exception(f"Error performing PATCH request to {url}: {e}")

    async def put(self, url, data=None, json=None, headers=None):
        """
        Perform an HTTP PUT request.
        :param url: The endpoint URL.
        :param data: Form data.
        :param json: JSON data.
        :param headers: Request headers.
        :return: The response
        """
        try:
            async with self._session.put(url, data=data, json=json, headers=headers) as response:
                return await self._handle_response(response)
        except Exception as e:
            raise Exception(f"Error performing PUT request to {url}: {e}")

    async def delete(self, url, headers=None):
        """
        Perform an HTTP DELETE request.
        :param url: The endpoint URL.
        :param headers: Request headers.
        :return: The response
        """
        try:
            async with self._session.delete(url, headers=headers) as response:
                return await self._handle_response(response)
        except Exception as e:
            raise Exception(f"Error performing DELETE request to {url}: {e}")
