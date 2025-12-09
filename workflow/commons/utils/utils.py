from functools import wraps
from typing import Optional, TypeVar

T = TypeVar('T')


def map_optional(optional_data: Optional[T], map_function):
    if optional_data is None:
        return None
    else:
        return map_function(optional_data)
