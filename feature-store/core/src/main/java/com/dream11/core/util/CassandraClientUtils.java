package com.dream11.core.util;

import static com.dream11.core.constant.Constants.DEFAULT_TENANT_NAME;

import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import io.vertx.cassandra.CassandraClientOptions;
import io.vertx.reactivex.cassandra.CassandraClient;
import io.vertx.reactivex.core.Vertx;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CassandraClientUtils {

  public static CassandraClient createClient(Vertx vertx, String defaultHostname, String key) {
    String hostname = getTenantCassandraHostName(defaultHostname, key);

    CassandraClientOptions options = new CassandraClientOptions();
    options.setContactPoints(List.of(hostname)).setPort(9042);

    if (Objects.equals(System.getProperty("app.environment"), "test")
        || Objects.equals(System.getProperty("app.environment"), "local"))
      options.setPort(Integer.parseInt(System.getProperty("cassandra.tenant." + key + ".port", "9042")));
    
    PoolingOptions poolingOptions = options.dataStaxClusterBuilder().getConfiguration().getPoolingOptions();
    poolingOptions.setMaxRequestsPerConnection(HostDistance.LOCAL, 10000);
    poolingOptions.setMaxRequestsPerConnection(HostDistance.REMOTE, 10000);
    poolingOptions.setMaxQueueSize(1024);
    options.dataStaxClusterBuilder().withPoolingOptions(poolingOptions);
    options.dataStaxClusterBuilder().withoutJMXReporting();

    return CassandraClient.createShared(vertx, key, options);
  }

  public static String getTenantCassandraHostName(String genericHostName, String key) {
    if (Objects.equals(System.getProperty("app.environment"), "test")
        || Objects.equals(System.getProperty("app.environment"), "local"))
      return System.getProperty("cassandra.tenant." + key + ".host", "localhost");

    if (Objects.equals(key, DEFAULT_TENANT_NAME)) {
      key = "";
    }
    return genericHostName.replace("{{KEY}}", Objects.equals(key, "") ? "" : "-" + key);
  }
}
