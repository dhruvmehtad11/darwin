package darwincatalog.testcontainers;

import org.testcontainers.containers.MySQLContainer;

public class SharedMysqlContainer extends MySQLContainer<SharedMysqlContainer> {
  private static final String IMAGE_VERSION = "mysql:8.0.32";
  private static SharedMysqlContainer container;

  private SharedMysqlContainer() {
    super(IMAGE_VERSION);

    withDatabaseName("asset");
    withUsername("root");
    withPassword("root");
  }

  public static SharedMysqlContainer getInstance() {
    // singleton as this will be shared across all IT tests
    if (container == null) {
      container = new SharedMysqlContainer();
      container.start();
    }
    return container;
  }

  @Override
  public void stop() {
    // prevent shutdown between tests
  }
}
