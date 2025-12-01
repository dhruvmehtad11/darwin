from typing import Optional

from pydantic import BaseModel


class JupyterRequest(BaseModel):
    jupyter_path: str
    release_name: str
    cloud_env: Optional[str] = None


class JupyterPodRequest(BaseModel):
    consumer_id: str
