import asyncio
from functools import wraps
from typing import Any, Callable
from src.app_layer import inference_executor


def run_in_executor() -> Callable:
    """
    Creates a decorator that runs a synchronous function in the provided thread executor

    Returns:
        Decorator function that wraps sync functions to run in executor
    """

    def decorator(func: Callable) -> Callable:
        @wraps(func)
        async def wrapper(self: Any, *args: Any, **kwargs: Any) -> Any:
            executor = inference_executor
            # Create a lambda that captures kwargs properly
            wrapped_func = lambda: func(self, *args, **kwargs)
            return await asyncio.get_running_loop().run_in_executor(executor, wrapped_func)

        return wrapper

    return decorator
