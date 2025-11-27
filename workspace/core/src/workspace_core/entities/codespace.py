from dataclasses import *
from typeguard import typechecked
from typing import Optional


@dataclass
@typechecked
class Codespace:
    project_id: int
    name: str
    user: str
    sync_location: Optional[str]
    cluster_id: Optional[str]
    jupyter_link: Optional[str]

    def __init__(
        self,
        project_id: int,
        name: str,
        user: str,
        sync_location: str = None,
        cluster_id: str = None,
        jupyter_link: str = None,
    ):
        self.project_id = project_id
        self.name = name
        self.user = user
        self.sync_location = sync_location
        self.cluster_id = cluster_id
        self.jupyter_link = jupyter_link
