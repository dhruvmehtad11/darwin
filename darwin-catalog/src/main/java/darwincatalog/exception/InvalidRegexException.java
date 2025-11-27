package darwincatalog.exception;

public class InvalidRegexException extends AssetException {
  public InvalidRegexException(String reason) {
    super("Invalid regex: " + reason);
  }
}
