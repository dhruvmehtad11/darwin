from src.dto.BaseError import ServiceError, BaseError


async def custom_exception_handler(request, exc):
    base_error: BaseError
    if type(exc) == ServiceError:
        base_error = BaseError(exc)
    else:
        base_error = BaseError.get_base_error(exc)

    return base_error.get_response()
