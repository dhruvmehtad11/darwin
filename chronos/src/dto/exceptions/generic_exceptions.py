from loguru import logger as logging
from datetime import datetime


class GenericException(Exception):
    def __init__(self, message):
        super().__init__(message)
        self.message = message
        self.when = datetime.now()
        logging.exception(message, self.when)


class BadConsumerConfigException(GenericException):
    def __init__(self, message):
        super().__init__(message)
        self.message = message
        self.when = datetime.now()
        logging.exception(message, self.when)


class BadInput(GenericException):
    def __init__(self, message):
        super().__init__(message)
        self.message = message
        self.when = datetime.now()
        logging.exception(message, self.when)


class RecoverableException(GenericException):
    """
    This is to be used for cases where the consumer offset should not be committed and message should be reprocessed,
    example of such cases are service unavailability (5xx) or rate limit (429)
    """

    def __init__(self, status_code, message):
        super().__init__(message)
        self.message = message
        self.status_code = status_code
        self.when = datetime.now()
        logging.exception(message, self.when, self.status_code)


class NonRecoverableException(GenericException):
    """
    Exception classified to non-recoverable should be moved to dlq like de-serialization,
    invalid-data causing internal server error
    """

    def __init__(self, status_code, message):
        super().__init__(message)
        self.message = message
        self.status_code = status_code
        self.when = datetime.now()
        logging.exception(message, self.when, self.status_code)
