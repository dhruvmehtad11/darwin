from fastapi.responses import JSONResponse

UNKNOWN_EXCEPTION_CODE = "UNKNOWN_EXCEPTION"
MYSQL_EXCEPTION_CODE = "MYSQL_EXCEPTION"
EVENT_NOT_FOUND_EXCEPTION_CODE = "EVENT_NOT_FOUND_EXCEPTION"
EVENT_SEARCH_EXCEPTION_CODE = "EVENT_SEARCH_EXCEPTION"
ENTITY_RELATION_EXCEPTION_CODE = "ENTITY_RELATION_EXCEPTION"
ENTITY_RELATION_NOT_FOUND_EXCEPTION_CODE = "ENTITY_RELATION_NOT_FOUND_EXCEPTION"
ENTITY_RELATION_ALREADY_EXISTS_EXCEPTION_CODE = "ENTITY_RELATION_ALREADY_EXISTS_EXCEPTION"


class ServiceError(Exception):
    code: str
    message: str
    status_code: int

    def __init__(self, code: str, message: str, status_code: int):
        self.code = code
        self.message = message
        self.status_code = status_code

    @staticmethod
    def get_error(e: Exception, error_code: str = UNKNOWN_EXCEPTION_CODE, status_code: int = 500):
        return ServiceError(error_code, str(e), status_code)


class BaseError:
    error: ServiceError

    def __init__(self, error: ServiceError):
        self.error = error

    def get_response(self) -> JSONResponse:
        return JSONResponse(BaseError.get_json(self.error), self.error.status_code)

    @staticmethod
    def get_json(error: ServiceError):
        return {
            "error": {
                "code": error.code,
                "message": error.message,
                "cause": error.message
            }
        }

    @staticmethod
    def get_base_error(e: Exception):
        return BaseError(ServiceError.get_error(e))
