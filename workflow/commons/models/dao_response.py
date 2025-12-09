from dataclasses import dataclass
from typing_extensions import Literal
from typing import TypeVar, Generic

T = TypeVar('T')
OPERATION = Literal["CREATE", "READ", "UPDATE", "DELETE", "SEARCH", "CREATE_MAPPING"]
STATUS = Literal["SUCCESS", "ERROR"]


@dataclass
class DaoResponse(Generic[T]):
    """Response given by the the DAO object."""

    operation: OPERATION
    status: STATUS
    message: str
    data: T

    def __init__(self, operation: OPERATION, status: STATUS, message: str, data: T):
        self.operation = operation
        self.status = status
        self.message = message
        self.data = data

    @classmethod
    def success_response(cls, operation: OPERATION, data: T):
        """Class Constructor for success response"""
        return cls(operation, "SUCCESS", "", data)

    @classmethod
    def success_response_with_message(cls, operation: OPERATION, data: T, message: str):
        """Class Constructor for success response with message"""
        return cls(operation, "SUCCESS", message, data)

    @classmethod
    def error_response(cls, operation: OPERATION, message: str):
        """Class Constructor for error response"""
        return cls(operation, "ERROR", message, None)
