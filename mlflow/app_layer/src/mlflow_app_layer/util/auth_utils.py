from mlflow_app_layer.constant.config import Config
import base64

from mlflow_app_layer.util.logging_util import get_logger

logger = get_logger(__name__)


def get_authorization_header(config: Config):
    """
    Get authorization header from config credentials.
    Returns None if credentials are missing or invalid.
    """
    try:
        credentials = config.mlflow_admin_credentials()
        username = credentials.get("username")
        password = credentials.get("password")
        
        # Check if credentials are valid (not None or empty)
        if not username or not password or username == "None" or password == "None":
            logger.warning("MLflow admin credentials are missing or invalid. Skipping auth header.")
            return None
            
        logger.debug("Using MLflow admin credentials for authentication")
        basic_auth_str = ("%s:%s" % (username, password)).encode("utf-8")
        return "Basic " + base64.standard_b64encode(basic_auth_str).decode("utf-8")
    except Exception as e:
        logger.error("Error generating authorization header: %s", str(e))
        return None
