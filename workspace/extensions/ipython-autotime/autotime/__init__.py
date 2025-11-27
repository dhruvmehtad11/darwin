from __future__ import print_function

import pytz
from ._version import version as __version__
from datetime import datetime
from time import localtime, mktime

try:
    from time import monotonic
except ImportError:
    from monotonic import monotonic

from IPython.core.magics.execution import _format_time as format_delta


def format_timestamp(struct_time):
    # Convert struct_time to a timestamp (seconds since epoch) if necessary
    if isinstance(struct_time, tuple):  # For `time.struct_time`
        timestamp_seconds = mktime(struct_time)
    else:  # Assume it's already a timestamp
        timestamp_seconds = struct_time

    # Convert the timestamp to a datetime object
    naive_datetime = datetime.fromtimestamp(timestamp_seconds)

    # Set the timezone to IST (Indian Standard Time)
    ist = pytz.timezone("Asia/Kolkata")
    ist_time = naive_datetime.astimezone(ist)

    # Format the IST time with a colon in the timezone offset
    timestamp_str = ist_time.strftime("%Y-%m-%d %H:%M:%S %z")
    return "{}:{}".format(timestamp_str[:-2], timestamp_str[-2:])


class LineWatcher(object):
    """Class that implements a basic timer.

    Notes
    -----
    * Register the `start` and `stop` methods with the IPython events API.
    """

    __slots__ = ["start_time", "timestamp"]

    def start(self, *args, **kwargs):
        self.timestamp = localtime()
        self.start_time = monotonic()

    def stop(self, *args, **kwargs):
        delta = monotonic() - self.start_time
        print(
            "time: {} (started: {})".format(
                format_delta(delta),
                format_timestamp(self.timestamp),
            )
        )


timer = LineWatcher()
start = timer.start
stop = timer.stop


def load_ipython_extension(ip):
    start()
    ip.events.register("pre_run_cell", start)
    ip.events.register("post_run_cell", stop)


def unload_ipython_extension(ip):
    ip.events.unregister("pre_run_cell", start)
    ip.events.unregister("post_run_cell", stop)
