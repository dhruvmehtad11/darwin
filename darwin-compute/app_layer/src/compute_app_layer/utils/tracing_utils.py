import time

from fastapi.requests import Request
from fastapi.responses import Response
from loguru import logger
from opentelemetry import trace
from opentelemetry.trace import Status, StatusCode
from starlette.middleware.base import BaseHTTPMiddleware
from typing import Callable

from compute_app_layer import TELEMETRY_ENABLED

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
