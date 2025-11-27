package darwincatalog.exception;

public class InvalidSqlException extends AssetException {
  public InvalidSqlException() {
    super();
  }

  public InvalidSqlException(String sql) {
    super("Invalid SQL: " + sql);
  }
}
