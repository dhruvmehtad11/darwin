from fastapi import FastAPI
from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor

def otel_instrumentor(app:FastAPI):
    try:
        FastAPIInstrumentor.instrument_app(app)
    except ImportError:
        pass