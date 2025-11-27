package darwincatalog.exception;

import com.datadog.api.client.ApiException;

public class DatadogException extends RuntimeException {
  public DatadogException(ApiException e) {
    super(e);
  }
}
