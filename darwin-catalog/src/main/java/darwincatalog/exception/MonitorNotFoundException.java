package darwincatalog.exception;

public class MonitorNotFoundException extends AssetException {
  public MonitorNotFoundException(Long id) {
    super(String.format("Monitor with id %s not found", id));
  }
}
