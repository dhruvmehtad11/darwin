package darwincatalog.exception;

import java.util.List;
import java.util.Set;

public class InvalidAttributeException extends AssetException {
  public InvalidAttributeException(String message) {
    super(message);
  }

  public InvalidAttributeException(List<String> invalidAttributes, Set<String> validAttributes) {
    this(
        String.format(
            "Attributes %s are invalid for an asset. List of valid attributes are: %s",
            invalidAttributes, validAttributes));
  }
}
