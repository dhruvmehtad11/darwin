package darwincatalog.exception;

import org.openapitools.model.AssetType;

public class InvalidMetricException extends AssetException {
  public InvalidMetricException(String metricName, AssetType assetType) {
    super(String.format("Invalid metric '%s' for type %s", metricName, assetType));
  }
}
