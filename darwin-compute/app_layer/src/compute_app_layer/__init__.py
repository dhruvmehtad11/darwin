import json
import os

from contextvars import ContextVar
from loguru import logger
from opentelemetry.sdk.resources import SERVICE_NAME, Resource
from opentelemetry import trace, metrics
from opentelemetry.exporter.otlp.proto.http.trace_exporter import OTLPSpanExporter
from opentelemetry.exporter.otlp.proto.http.metric_exporter import OTLPMetricExporter
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.sdk.trace.sampling import TraceIdRatioBased, ParentBased
from opentelemetry.sdk.metrics import MeterProvider
from opentelemetry.sdk.metrics.export import PeriodicExportingMetricReader


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
        "time": record["time"].strftime("%Y-%m-%d %H:%M:%S.%f")[:-3] if record["time"] else "",
        "level": record["level"].name if record["level"] else "",
        "request_id": record["extra"].get("request_id", "no-request-id"),
        "name": record["name"] if "name" in record else "",
        "function": record["function"] if "function" in record else "",
        "line": record["line"] if "line" in record else "",
        "message": record["message"].replace("{", "{{").replace("}", "}}") if "message" in record else "",
    }
    return json.dumps(log_record)


# Custom formatter to include request_id in the log messages
def custom_formatter(record):
    request_id = request_id_var.get()
    record["extra"]["request_id"] = request_id

    # Escape curly braces in the message
    record["message"] = record["message"].replace("{", "{{").replace("}", "}}")

    # Use the logger_format to format the record
    formatted_message = logger_format.format_map(record)
    return formatted_message


logger.remove()  # remove stdout logger

log_file = os.getenv("LOG_FILE", "debug.log")
print("Printing the logs to the directory :", log_file)

# Configure logger to use the custom formatter
logger.add(
    sink=log_file,
    format=custom_formatter,
    rotation="1000 MB",  # Rotate when file exceeds 1000 MB
    retention="10 days",  # Keep rotated logs for 10 days
    backtrace=True,
    diagnose=True,
)

service_name = os.getenv("OTEL_SERVICE_NAME", "darwin-compute")
endpoint = os.getenv("OTEL_EXPORTER_OTLP_ENDPOINT", "http://localhost:4318")

# Sampling rate configuration (0.0 to 1.0)
# 1.0 = 100% (all traces), 0.1 = 10% (sample 10% of traces)
try:
    sampling_rate = float(os.getenv("OTEL_TRACES_SAMPLER_RATIO", "1.0"))
    # Clamp between 0.0 and 1.0
    sampling_rate = max(0.0, min(1.0, sampling_rate))
except (ValueError, TypeError):
    print("Invalid OTEL_TRACES_SAMPLER_RATIO value, defaulting to 1.0 (100%)")
    sampling_rate = 1.0

# Flag to track if telemetry is enabled
TELEMETRY_ENABLED = False

# Allow disabling telemetry via environment variable
telemetry_disabled = os.getenv("OTEL_DISABLED", "false").lower() == "true"

if telemetry_disabled:
    logger.info("OpenTelemetry is disabled via OTEL_DISABLED environment variable")
    TELEMETRY_ENABLED = False
else:
    try:
        resource = Resource.create({SERVICE_NAME: service_name})

        # --- Traces with Sampling ---
        # Use ParentBased sampler with TraceIdRatioBased for consistent sampling
        sampler = ParentBased(root=TraceIdRatioBased(sampling_rate))
        tracer_provider = TracerProvider(resource=resource, sampler=sampler)
        tracer_provider.add_span_processor(BatchSpanProcessor(OTLPSpanExporter(endpoint=f"{endpoint}/v1/traces")))
        trace.set_tracer_provider(tracer_provider)

        # --- Metrics ---
        reader = PeriodicExportingMetricReader(OTLPMetricExporter(endpoint=f"{endpoint}/v1/metrics"))
        metrics.set_meter_provider(MeterProvider(resource=resource, metric_readers=[reader]))

        TELEMETRY_ENABLED = True
        print(
            f"OpenTelemetry initialized successfully. Service: {service_name}, Endpoint: {endpoint}, Sampling Rate: {sampling_rate * 100:.1f}%"
        )
    except Exception as e:
        print(f"Failed to initialize OpenTelemetry: {e}. Application will continue without tracing.")
        TELEMETRY_ENABLED = False
