# otel_bootstrap.py
import os
from opentelemetry.sdk.resources import SERVICE_NAME, Resource
from opentelemetry import trace, metrics
from opentelemetry.exporter.otlp.proto.http.trace_exporter import OTLPSpanExporter
from opentelemetry.exporter.otlp.proto.http.metric_exporter import OTLPMetricExporter
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.sdk.metrics import MeterProvider
from opentelemetry.sdk.metrics.export import PeriodicExportingMetricReader
from loguru import logger


def _str_to_bool(value: str) -> bool:
    """Convert string environment variable to boolean."""
    return value.lower() in ("true", "1", "yes", "on")


def should_enable_otel() -> bool:
    """
    Determine if OpenTelemetry should be enabled.
    
    By default:
    - Disabled for local development (ENV=local)
    - Enabled for all other environments (prod, staging, etc.)
    
    Can be explicitly controlled via ENABLE_OTEL environment variable.
    """
    enable_otel = os.getenv("ENABLE_OTEL", "").strip()
    
    if enable_otel:
        return _str_to_bool(enable_otel)
    
    # Auto-detect based on ENV
    env = os.getenv("ENV", "local").lower()
    return env != "local"


# Check if telemetry should be enabled
otel_enabled = should_enable_otel()

if otel_enabled:
    logger.info("OpenTelemetry instrumentation enabled")
    
    service_name = os.getenv("OTEL_SERVICE_NAME", "darwin-ml-serve")
    endpoint = os.getenv("OTEL_EXPORTER_OTLP_ENDPOINT", "http://localhost:4318")
    
    resource = Resource.create({SERVICE_NAME: service_name})
    
    # --- Traces ---
    tracer_provider = TracerProvider(resource=resource)
    tracer_provider.add_span_processor(
        BatchSpanProcessor(OTLPSpanExporter(endpoint=f"{endpoint}/v1/traces"))
    )
    trace.set_tracer_provider(tracer_provider)
    
    # --- Metrics ---
    reader = PeriodicExportingMetricReader(
        OTLPMetricExporter(endpoint=f"{endpoint}/v1/metrics")
    )
    metrics.set_meter_provider(MeterProvider(resource=resource, metric_readers=[reader]))
else:
    logger.info("OpenTelemetry instrumentation disabled (local development mode)")