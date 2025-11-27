from darwin_fs.model.error_response import ApiError


class SdkException(Exception):
  def __init__(self, message, error_code=None):
    super().__init__(message)
    self.error_code = error_code

  def __str__(self):
    if self.error_code is not None:
      return f"[SDK_ERROR {self.error_code}] {super().__str__()}"
    return super().__str__()

class ApiException(Exception):
  def __init__(self, api_error: ApiError):
    super().__init__(api_error.message)
    self.message = api_error.message
    self.error_code = api_error.code

  def __str__(self):
    if self.error_code is not None:
      return f"[API_ERROR {self.error_code}] {super().__str__()}"
    return super().__str__()

class SparkWriterException(Exception):
  def __init__(self, message, error_code=None):
    super().__init__(message)
    self.message = message
    self.error_code = error_code

  def __str__(self):
    if self.error_code is not None:
      return f"[SPARK_ERROR {self.error_code}] {super().__str__()}"
    return super().__str__()