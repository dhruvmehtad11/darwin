package darwincatalog.exception;

public class InvalidRuleException extends AssetException {
  public InvalidRuleException(String reason) {
    super("Invalid rule: " + reason);
  }
}
