import traceback

from typing import Dict
from loguru import logger
from opentelemetry import metrics
from opentelemetry.metrics import Meter

from compute_core.constant.config import Config
from compute_script.util.darwin_slack_alert import DarwinSlackAlert


class CustomMetrics:
    """
    Utility class for emitting custom metrics to OpenTelemetry.
    Uses the globally configured MeterProvider (e.g. set by `opentelemetry-instrument`).
    """

    def __init__(self, env: str = None):
        """
        Initializes the metrics class.
        """
        self._config = Config(env)
        self._env = self._config.env
        self._meter: Meter = metrics.get_meter("darwin.compute.metrics")
        self._counters: Dict[str, object] = {}

    def increment(self, metric_name: str, tags: dict = None):
        """
        Increments a counter metric using OpenTelemetry metrics API.
        Args:
            metric_name (str): The name of the metric/instrument.
            tags (dict, optional): Attributes to attach to the datapoint.
        """
        try:
            if metric_name not in self._counters:
                # Create and cache a Counter instrument per metric_name
                self._counters[metric_name] = self._meter.create_counter(
                    name=metric_name, description=f"Counter for {metric_name}", unit="1"
                )

            attributes = {"env": self._env}
            if tags:
                attributes.update({str(k): str(v) for k, v in tags.items()})

            # Record increment
            self._counters[metric_name].add(1, attributes)
        except Exception as e:
            tb = traceback.format_exc()
            logger.error(f"Failed to increment metric '{metric_name}': {e}\n{tb}")
            DarwinSlackAlert().custom_metrics_error(metric_name, str(e), tb)
