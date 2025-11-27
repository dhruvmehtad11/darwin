from typing import Dict, Type
from src.listeners.base_listener import BaseListener


class ListenerRegistry:
    def __init__(self):
        self._registry: Dict[str, Type[BaseListener]] = {}

    def register(self, name: str, listener_cls: Type[BaseListener]):
        self._registry[name] = listener_cls

    def get_listener(self, name: str):
        listener_cls = self._registry.get(name)
        if not listener_cls:
            raise ValueError(f"Listener {name} not found")
        return listener_cls


listener_registry = ListenerRegistry()


def register_listener(name: str):
    def decorator(cls):
        listener_registry.register(name, cls)
        return cls

    return decorator
