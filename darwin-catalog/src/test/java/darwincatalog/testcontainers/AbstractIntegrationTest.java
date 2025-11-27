package darwincatalog.testcontainers;

import darwincatalog.testutils.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("testcontainer")
public abstract class AbstractIntegrationTest {

  protected TestUtil testUtil;

  private static SharedMysqlContainer mysqlContainer;

  @Autowired protected MockMvc mockMvc;

  public AbstractIntegrationTest() {
    this.testUtil = new TestUtil();
  }

  @BeforeEach
  void beforeEach() throws Exception {
    testUtil.setMockMvc(mockMvc);
  }

  private static SharedMysqlContainer getMysqlContainer() {
    if (mysqlContainer == null) {
      mysqlContainer = SharedMysqlContainer.getInstance();
    }
    return mysqlContainer;
  }

  @DynamicPropertySource // Dynamically sets Spring properties to connect to the container
  static void setDatasourceProperties(DynamicPropertyRegistry registry) {
    SharedMysqlContainer container = getMysqlContainer();
    registry.add("spring.datasource.url", container::getJdbcUrl);
    registry.add("spring.datasource.username", container::getUsername);
    registry.add("spring.datasource.password", container::getPassword);
  }
}
