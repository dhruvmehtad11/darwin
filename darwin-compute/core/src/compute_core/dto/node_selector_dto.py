from dataclasses import dataclass


@dataclass
class NodeSelector:
    name: str
    value: str

    def __init__(self, name: str, value: str):
        self.name = name
        self.value = value
