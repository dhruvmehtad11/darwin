from dataclasses import dataclass


@dataclass
class LogCentralPodConfig:
    container_name: str
    service_name: str
    source: str

    def __init__(self, container_name: str, service_name: str, source: str):
        self.container_name = container_name
        self.service_name = service_name
        self.source = source
