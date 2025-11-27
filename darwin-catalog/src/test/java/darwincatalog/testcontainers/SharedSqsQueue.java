package darwincatalog.testcontainers;

import java.net.URI;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

public class SharedSqsQueue extends LocalStackContainer {
  private static final DockerImageName IMAGE = DockerImageName.parse("localstack/localstack:4.5.0");
  private static SharedSqsQueue container;

  private SharedSqsQueue() {
    super(IMAGE);
    withServices(Service.SQS);
  }

  public static SharedSqsQueue getInstance() {
    if (container == null) {
      container = new SharedSqsQueue();
      container.start();
    }
    return container;
  }

  public URI getEndpointOverride(Service service) {
    return super.getEndpointOverride(service);
  }

  public String getRegion() {
    return super.getRegion();
  }

  public String getAccessKey() {
    return super.getAccessKey();
  }

  public String getSecretKey() {
    return super.getSecretKey();
  }

  @Override
  public void stop() {
    // prevent shutdown between tests
  }
}
