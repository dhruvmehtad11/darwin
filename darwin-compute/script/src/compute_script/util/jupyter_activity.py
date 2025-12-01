from dataclasses import dataclass, field

import requests
from loguru import logger

from compute_core.util.utils import urljoin
from compute_script.util.recent_activity import recent_activity


@dataclass
class JupyterLabActivity:
    # expiry time in minutes
    expiry_time: int = field(default=5)

    def check_if_active(self, jupyter_link: str) -> bool:
        try:
            """
            Checks if the Last Activity of Jupyter Lab is more than the passed expiry time
            :return:
                true: Recent activity in the jupyter lab
                false: No Recent activity in the jupyter lab
            """
            content_url = urljoin(jupyter_link, "api/contents")

            logger.info(f"content_url {content_url}")
            s = requests.Session()
            contents_response = s.get(content_url)
            logger.info(f"contents_response before converting to json {contents_response}")
            contents_response = contents_response.json()
            logger.info(f"contents_response {contents_response}")

            if recent_activity(contents_response["last_modified"], self.expiry_time):
                return True

            return False
        except Exception as e:
            logger.exception(f"Error in check_if_active: {e}")
            return False

    def check_if_jupyter_up(self, jupyter_link: str) -> bool:
        try:
            logger.info(f"jupyter_link if it is up{jupyter_link}")
            content_url = urljoin(jupyter_link, "api/contents")

            logger.info(f"content_url {content_url}")
            s = requests.Session()
            contents_response = s.get(content_url)
            logger.info(f"contents_response before converting to json {contents_response}")
            contents_response = contents_response.json()
            logger.info(f"contents_response {contents_response}")
            return True
        except Exception as e:
            logger.exception(f"Error in check_if_active: {e}")
            return False
