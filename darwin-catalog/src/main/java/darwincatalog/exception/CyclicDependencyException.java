package darwincatalog.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CyclicDependencyException extends AssetException {
  public CyclicDependencyException(String parentAsset, String childAsset) {
    super(
        String.format(
            "Cyclic dependency detected between parent asset '%s' and child asset '%s'.",
            parentAsset, childAsset));
  }
}
