from enum import Enum
from typing import Any, Dict, Optional
import json
from aiohttp import ClientResponseError


class HermesException(Exception):
    """
    Custom exception class for Hermes errors that includes error details.

    Attributes:
        code (int): HTTP status code
        message (str): Error message
        error_hint (Optional[str]): Error hint code
        _custom_errors (Dict[str, 'HermesException']): Registry of custom error codes
    """

    _custom_errors: Dict[str, "HermesException"] = {}

    def __init__(self, code: int, message: str, error_hint: Optional[str] = None):
        self.code = code
        self.message = message
        self.error_hint = error_hint
        super().__init__(self._get_message())

    def __str__(self) -> str:
        base = f"{self.message} (Code: {self.code})"
        if self.error_hint:
            base = f"[{self.error_hint}] {base}"
        return base

    def _get_message(self) -> str:
        """Constructs the complete error message."""
        return str(self)

    def get_cli_error_message(self) -> str:
        """Returns a formatted error message with hint for CLI output."""
        if self.error_hint:
            return f"Error: {self.error_hint}"
        return f"Error: {self.message}"

    @classmethod
    def register_error(cls, error_code: str, http_code: int, message: str) -> "HermesException":
        """
        Register a new custom error code.

        Args:
            error_code (str): Unique error code identifier
            http_code (int): HTTP status code
            message (str): Error message

        Returns:
            HermesException: Newly created exception object

        Raises:
            ValueError: If error code already exists or parameters are invalid
        """
        if not error_code or not isinstance(error_code, str):
            raise ValueError("error_code must be a non-empty string")
        if not isinstance(http_code, int) or http_code < 100 or http_code > 599:
            raise ValueError("http_code must be a valid HTTP status code")
        if not message or not isinstance(message, str):
            raise ValueError("message must be a non-empty string")

        if error_code in cls._custom_errors:
            raise ValueError(f"Error code {error_code} already exists")

        error = cls(code=http_code, message=message, error_hint=error_code)
        cls._custom_errors[error_code] = error
        return error

    @classmethod
    def _parse_client_error_message(cls, client_response_error: ClientResponseError) -> str:
        """
        Parse error message from ClientResponseError.

        Args:
            client_response_error (ClientResponseError): The HTTP client error response

        Returns:
            str: Parsed error message
        """
        try:
            error_data = json.loads(client_response_error.message)
            error_message = error_data.get("message", [])

            if isinstance(error_message, list) and error_message:
                return error_message[0].get("msg", str(error_message))
            return str(error_message)

        except json.JSONDecodeError:
            return str(client_response_error.message)

    @classmethod
    def error_from_ClientResponseError(
        cls,
        error_code: "HermesErrorCodes",
        client_response_error: ClientResponseError,
        additional_hint: Optional[str] = None,
    ) -> "HermesException":
        """
        Create an HermesException instance from a ClientResponseError.

        Args:
            error_code (HermesErrorCodes): The predefined error code to use
            client_response_error (ClientResponseError): The HTTP client error response
            additional_hint (Optional[str]): Additional hint to append to the error message

        Returns:
            HermesException: A new instance with the error details from the response
        """
        error_hint = cls._parse_client_error_message(client_response_error)

        if additional_hint:
            error_hint = f"{error_hint} {additional_hint}"

        return cls(
            code=error_code.value.code,
            message=error_code.value.message,
            error_hint=error_hint,
        )

    @classmethod
    def get_error(cls, error_code: str) -> Optional["HermesException"]:
        """Retrieve a custom error by its code"""
        return cls._custom_errors.get(error_code)

    @classmethod
    def from_error_code(
        cls, error_code: "HermesErrorCodes", additional_hint: Optional[str] = None
    ) -> "HermesException":
        """
        Create a new HermesException instance from a predefined error code with optional additional hint.

        Args:
            error_code (HermesErrorCodes): The predefined error code to use
            additional_hint (Optional[str]): Additional hint to append to the error hint

        Returns:
            HermesException: A new instance with the predefined error details and modified hint
        """
        error_details = error_code.value
        error_hint = error_details.error_hint

        if additional_hint:
            error_hint = additional_hint

        return cls(
            code=error_details.code,
            message=error_details.message,
            error_hint=error_hint,
        )


class HermesErrorCodes(Enum):
    """Enum containing all error codes and their details"""

    # Authentication Errors (1xxx)
    UNAUTHORIZED = HermesException(401, "Unauthorized access", "AUTH_001")
    INVALID_TOKEN = HermesException(401, "Invalid authentication token", "AUTH_002")
    TOKEN_EXPIRED = HermesException(401, "Authentication token has expired", "AUTH_003")

    # Resource Errors (2xxx)
    RESOURCE_NOT_FOUND = HermesException(404, "Requested resource not found", "RES_001")
    RESOURCE_EXISTS = HermesException(409, "Resource already exists", "RES_002")
    RESOURCE_INVALID = HermesException(400, "Invalid resource format", "RES_003")

    # API Errors (3xxx)
    API_ERROR = HermesException(500, "Internal API error", "API_001")
    API_TIMEOUT = HermesException(504, "API request timed out", "API_002")
    API_UNAVAILABLE = HermesException(503, "API service unavailable", "API_003")

    # Validation Errors (4xxx)
    INVALID_REQUEST = HermesException(400, "Invalid request format", "VAL_001")
    MISSING_FIELD = HermesException(400, "Required field missing", "VAL_002")
    INVALID_FIELD = HermesException(400, "Invalid field value", "VAL_003")

    # Configuration Errors (5xxx)
    CONFIG_ERROR = HermesException(500, "Configuration error", "CFG_001")
    ENV_VAR_MISSING = HermesException(500, "Required environment variable missing", "CFG_002")
    INVALID_CONFIG = HermesException(500, "Invalid configuration", "CFG_003")


def handle_hermes_exception(e: Exception):
    """
    Handle different types of exceptions and convert them to appropriate HermesExceptions.

    Args:
        e (Exception): The exception to handle

    Raises:
        HermesException: Converted exception with appropriate error details
    """
    if isinstance(e, ClientResponseError):
        if e.status == 401:
            raise HermesException.error_from_ClientResponseError(HermesErrorCodes.UNAUTHORIZED, e)
        elif e.status == 404:
            raise HermesException.error_from_ClientResponseError(HermesErrorCodes.RESOURCE_NOT_FOUND, e)
        elif e.status == 400:
            raise HermesException.error_from_ClientResponseError(HermesErrorCodes.INVALID_REQUEST, e)
        elif e.status == 500:
            raise HermesException.error_from_ClientResponseError(
                HermesErrorCodes.API_ERROR,
                e,
                f"Failed to create artifact: {str(e)}",
            )
        else:
            raise HermesException.error_from_ClientResponseError(HermesErrorCodes.API_ERROR, e)
    raise e  # Re-raise unhandled exceptions
