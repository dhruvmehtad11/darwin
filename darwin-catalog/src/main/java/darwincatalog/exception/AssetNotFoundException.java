package darwincatalog.exception;

public class AssetNotFoundException extends AssetException {
  public AssetNotFoundException(String name) {
    super("Asset with name " + name + " not found");
  }

  public AssetNotFoundException(Long id) {
    super("Asset with id " + id + " not found");
  }
}
