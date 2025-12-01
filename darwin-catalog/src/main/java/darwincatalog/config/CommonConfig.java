package darwincatalog.config;

import com.datadog.api.client.ApiClient;
import com.datadog.api.client.v1.api.MonitorsApi;
import com.datadog.api.client.v2.api.MetricsApi;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class CommonConfig {

  @Bean
  @Primary
  public ObjectMapper customMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    return mapper;
  }

  @Bean
  public ObjectMapper lenientMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    return mapper;
  }

  @Bean
  public ApiClient datadogClient(Properties properties) {
    ApiClient datadogClient = ApiClient.getDefaultApiClient();
    Map<String, String> map = new HashMap<>();
    map.put("apiKeyAuth", properties.getDatadogApiKey());
    map.put("appKeyAuth", properties.getDatadogAppKey());
    datadogClient.configureApiKeys(map);
    return datadogClient;
  }

  @Bean
  public MetricsApi metricsApi(ApiClient datadogClient) {
    return new MetricsApi(datadogClient);
  }

  @Bean
  public MonitorsApi monitorsApi(ApiClient datadogClient) {
    return new MonitorsApi(datadogClient);
  }

  @Bean
  public AwsCredentialsProvider getAwsCredentialsProvider() {
    return DefaultCredentialsProvider.create();
  }

  @Bean
  public GlueClient getGlueClient(AwsCredentialsProvider awsCredentialsProvider) {
    return GlueClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(awsCredentialsProvider)
        .build();
  }

  @Bean
  public SqsClient sqsClient(AwsCredentialsProvider awsCredentialsProvider) {
    return SqsClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(awsCredentialsProvider)
        .build();
  }
}
