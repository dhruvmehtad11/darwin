# otel_bootstrap.py
import os
import time
import logging
from typing import Callable
from opentelemetry.sdk.resources import SERVICE_NAME, Resource
from opentelemetry import trace, metrics
from opentelemetry.exporter.otlp.proto.http.trace_exporter import OTLPSpanExporter
from opentelemetry.exporter.otlp.proto.http.metric_exporter import OTLPMetricExporter
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.sdk.trace.sampling import TraceIdRatioBased, ParentBased
from opentelemetry.sdk.metrics import MeterProvider
from opentelemetry.sdk.metrics.export import PeriodicExportingMetricReader
from opentelemetry.trace import Status, StatusCode
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import Response

logger = logging.getLogger(__name__)

service_name = os.getenv("OTEL_SERVICE_NAME", "darwin-workspace")
endpoint = os.getenv("OTEL_EXPORTER_OTLP_ENDPOINT", "http://localhost:4318")

# Sampling rate configuration (0.0 to 1.0)
# 1.0 = 100% (all traces), 0.1 = 10% (sample 10% of traces)
try:
    sampling_rate = float(os.getenv("OTEL_TRACES_SAMPLER_RATIO", "1.0"))
    # Clamp between 0.0 and 1.0
    sampling_rate = max(0.0, min(1.0, sampling_rate))
except (ValueError, TypeError):
    logger.warning("Invalid OTEL_TRACES_SAMPLER_RATIO value, defaulting to 1.0 (100%)")
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
        logger.info(
            f"OpenTelemetry initialized successfully. "
            f"Service: {service_name}, Endpoint: {endpoint}, "
            f"Sampling Rate: {sampling_rate * 100:.1f}%"
        )

    except Exception as e:
        logger.warning(f"Failed to initialize OpenTelemetry: {e}. Application will continue without tracing.")
        TELEMETRY_ENABLED = False

# Get tracer for middleware (will be a no-op tracer if initialization failed)
tracer = trace.get_tracer(__name__)


class OpenTelemetryMiddleware(BaseHTTPMiddleware):
    """
    FastAPI middleware to automatically trace all HTTP requests.
    This adds a span for each request with relevant HTTP attributes.

    Designed to be resilient - if tracing fails, the API request continues normally.
    """

    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        # If telemetry is disabled or fails, just process the request normally
        if not TELEMETRY_ENABLED:
            return await call_next(request)

        # Wrap everything in try-except to ensure tracing errors don't break the API
        try:
            # Create span name from HTTP method and route
            span_name = f"{request.method} {request.url.path}"

            with tracer.start_as_current_span(span_name) as span:
                start_time = time.time()

                try:
                    # Add HTTP attributes to span (wrapped in try-except)
                    try:
                        span.set_attribute("http.method", request.method)
                        span.set_attribute("http.url", str(request.url))
                        span.set_attribute("http.route", request.url.path)
                        span.set_attribute("http.scheme", request.url.scheme)

                        # Add headers as attributes (optional, be careful with sensitive data)
                        if "user-agent" in request.headers:
                            span.set_attribute("http.user_agent", request.headers["user-agent"])

                        # Add custom user header if present
                        if "msd-user" in request.headers:
                            span.set_attribute("user.id", request.headers["msd-user"])
                    except Exception as attr_error:
                        logger.debug(f"Failed to set span attributes: {attr_error}")

                    # Process the request
                    response = await call_next(request)

                    # Add response attributes (wrapped in try-except)
                    try:
                        span.set_attribute("http.status_code", response.status_code)

                        # Set span status based on HTTP status code
                        if response.status_code >= 400:
                            span.set_status(Status(StatusCode.ERROR))
                        else:
                            span.set_status(Status(StatusCode.OK))

                        # Add duration attribute
                        duration = time.time() - start_time
                        span.set_attribute("http.duration_ms", duration * 1000)
                    except Exception as resp_attr_error:
                        logger.debug(f"Failed to set response attributes: {resp_attr_error}")

                    return response

                except Exception as e:
                    # Record exception in span (if possible)
                    try:
                        span.record_exception(e)
                        span.set_status(Status(StatusCode.ERROR, str(e)))
                    except Exception as span_error:
                        logger.debug(f"Failed to record exception in span: {span_error}")
                    raise

        except Exception as tracing_error:
            # If anything goes wrong with tracing, log it but don't break the API
            logger.warning(f"Tracing error in middleware: {tracing_error}. Processing request without tracing.")
            return await call_next(request)
