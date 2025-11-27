from datetime import timedelta

from dateutil.parser import parse


def format_time_to_ist(time_to_format: str, final_format: str = "%d-%b-%Y %H:%M:%S"):
    """
    Convert time in str from ust to ist from some initial_format to final_format
    :params time_to_format: any time in str
    :params final_format: Final format of time string. Default: "%d-%b-%Y %H:%M:%S"
    :returns: time in ist in final format
    """
    iso_time = parse(time_to_format) + timedelta(hours=5, minutes=30)
    return iso_time.strftime(final_format)
