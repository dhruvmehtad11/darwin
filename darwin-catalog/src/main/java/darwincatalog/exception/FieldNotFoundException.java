package darwincatalog.exception;

public class FieldNotFoundException extends AssetException {
  public FieldNotFoundException(long schemaEntityId, String name) {
    super(
        String.format("Field with name %s does not exist for schema id %s", name, schemaEntityId));
  }
}
