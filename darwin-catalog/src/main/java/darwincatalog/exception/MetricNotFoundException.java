package darwincatalog.exception;

public class MetricNotFoundException extends AssetException {
  public MetricNotFoundException(String name) {
    super("Metric with name " + name + " not found");
  }
}
