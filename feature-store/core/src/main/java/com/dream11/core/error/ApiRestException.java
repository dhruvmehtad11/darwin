package com.dream11.core.error;

import com.dream11.rest.exception.RestException;
import com.dream11.rest.io.Error;

public class ApiRestException extends RestException {

  public ApiRestException(ServiceError error) {
    super(error.getErrorMessage(), error.getError(), error.getHttpStatusCode());
  }

  public ApiRestException(Throwable throwable, ServiceError error) {
    super(throwable, error.getError(), error.getHttpStatusCode());
  }

  //  public ApiRestException(Throwable throwable, ServiceError error) {
  //    super(throwable, error.getError(), error.getHttpStatusCode());
  //  }

  public ApiRestException(String message, ServiceError error) {
    super(
        java.util.Optional.ofNullable(message).orElse(error.getErrorMessage()),
        Error.of(error.getErrorCode(), message),
        error.getHttpStatusCode());
  }

  public ApiRestException(String message, ServiceError error, int httpStatusCode) {
    super(
            java.util.Optional.ofNullable(message).orElse(error.getErrorMessage()),
            Error.of(error.getErrorCode(), message),
            httpStatusCode);
  }
}
