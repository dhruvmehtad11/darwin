import os
import traceback

from loguru import logger

logger_format = (
    "{time:YYYY-MM-DD HH:mm:ss} | "
    "{level:<8} | "
    "{name}:{function}:{line} | "
    "{message}\n"
)

def custom_formatter(record):
    record["message"] = record["message"].replace("{", "{{").replace("}", "}}")

    formatted_message = logger_format.format_map(record)
    if record["exception"]:
        exc_type, exc_value, exc_tb = record["exception"]
        formatted_message += "".join(traceback.format_exception(exc_type, exc_value, exc_tb))

    return formatted_message


logger.remove()

log_file = os.getenv("LOG_FILE", "debug.log")

logger.add(
    log_file,
    format=custom_formatter,
    rotation="1000 MB",      # Rotate when file exceeds 1000 MB
    retention="1 days",    # Keep rotated logs for 1 days
    backtrace=True,
    diagnose=True
)
