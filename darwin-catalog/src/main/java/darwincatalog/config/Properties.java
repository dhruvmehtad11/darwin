package darwincatalog.config;

import java.util.List;
import java.util.Set;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class Properties {
  @Value("${contractcentral.datadog.app.key}")
  private String datadogAppKey;

  @Value("${contractcentral.datadog.api.key}")
  private String datadogApiKey;

  @Value("${contractcentral.aws.sqs.glue-ddl-queue}")
  private String glueListenerQueueUrl;

  @Value("${contractcentral.ddl.listener.enabled}")
  private boolean glueListenerEnabled;

  @Value("${contractcentral.consumer.token.expiry}")
  private Long consumerTokenExpiry;

  @Value("${spring.profiles.active:default}")
  private String springProfile;

  @Value("${contractcentral.datadog.monitor.type}")
  private String datadogDefaultMonitorType;

  @Value("${contractcentral.datadog.monitor.custom.webhooks}")
  private List<String> datadogCustomWebhooks;

  // this is a datadog limitation! Having env and environment as different mandatory tags
  @Value("${contractcentral.datadog.monitor.env}")
  private String datadogMonitorEnv;

  @Value("${contractcentral.datadog.monitor.environment}")
  private String datadogMonitorEnvironment;

  @Value("${contractcentral.datadog.monitor.slack.webhooks}")
  private List<String> datadogSlackWebhooks;

  @Value("${contractcentral.consumer.token.self}")
  private Set<String> selfTokenNames;

  @Value("${contractcentral.application.name}")
  private String applicationName;

  @Value("${contractcentral.aws.redshift.jdbc.url:}")
  private String redshiftJdbcUrl;

  @Value("${contractcentral.aws.redshift.username:}")
  private String redshiftUsername;

  @Value("${contractcentral.aws.redshift.password:}")
  private String redshiftPassword;

  @Value("${contractcentral.ddl.listener}")
  private String ddlListenerType;
}
