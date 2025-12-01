from datetime import timedelta, datetime

from dateutil.parser import parse


def recent_activity(time_to_check: str, ideal_time: int) -> bool:
    """
    Checks if time passed from time_to_check to now is less than time_to_expiry
    :param time_to_check: Any timestamp
    :param ideal_time: Time duration to compare with in minutes
    :return: true/false
    """
    time_to_check = parse(time_to_check).replace(tzinfo=None)
    ideal_time = timedelta(minutes=ideal_time)
    now = datetime.utcnow()
    time_passed = now - time_to_check
    return time_passed < ideal_time
