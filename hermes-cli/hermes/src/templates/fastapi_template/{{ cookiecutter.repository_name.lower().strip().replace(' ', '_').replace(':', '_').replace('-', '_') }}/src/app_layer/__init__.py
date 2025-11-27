import json
import os
from contextvars import ContextVar
from concurrent.futures import ThreadPoolExecutor
from loguru import logger
from src.config.constants import MAX_WORKERS


# Define a context variable for request_id
request_id_var = ContextVar("request_id", default="default")

# Define the plain text logger format
logger_format = (
    "{time:YYYY-MM-DD HH:mm:ss.SSS} | "
    "{level:<8} | "
    "{extra[request_id]:<15} | "
    "{name}:{function}:{line} | "
    "{message}\n"
)


# JSON formatter for loguru
def json_formatter(record):
    request_id = request_id_var.get()
    record["extra"]["request_id"] = request_id
    # Create a dictionary with the log information
    log_record = {
        "time": (record["time"].strftime("%Y-%m-%d %H:%M:%S.%f")[:-3] if record["time"] else ""),
        "level": record["level"].name if record["level"] else "",
        "request_id": record["extra"].get("request_id", "no-request-id"),
        "name": record["name"] if "name" in record else "",
        "function": record["function"] if "function" in record else "",
        "line": record["line"] if "line" in record else "",
        "message": record["message"] if "message" in record else "",
    }
    return json.dumps(log_record)


# Custom formatter to include request_id in the log messages
def custom_formatter(record):
    request_id = request_id_var.get()
    record["extra"]["request_id"] = request_id

    # Escape curly braces in the message
    record["message"] = record["message"]

    # Use the logger_format to format the record
    formatted_message = logger_format.format_map(record)
    return formatted_message


logger.remove()  # remove stdout logger

log_file = os.getenv("LOG_FILE", "debug.log")
print("Printing the logs to the directory :", log_file)

# Configure logger to use the custom formatter
logger.configure(handlers=[{"sink": log_file, "format": custom_formatter}])


inference_executor = ThreadPoolExecutor(max_workers=MAX_WORKERS)
