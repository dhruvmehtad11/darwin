package com.dream11.admin.crudintegrationteststtltests;

import static com.dream11.admin.expectedrequestresponse.CassandraEntityTestData.*;
import static com.dream11.admin.expectedrequestresponse.CassandraEntityTtlTestData.SuccessFeatureGroupTtlUpdateTest;
import static com.dream11.admin.expectedrequestresponse.CassandraFeatureGroupTestData.MoveBackFeatureGroupTenantTest;
import static com.dream11.core.util.Utils.getAndAssertResponse;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import io.vertx.core.json.JsonObject;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SetUp.class)
public class RestIT {

  // entity tests
  @Test
  public void successFeatureGroupTtlUpdateTest() {
    JsonObject expectedResponse = SuccessFeatureGroupTtlUpdateTest.getJsonObject("response");
    getAndAssertResponse(
        SuccessFeatureGroupTtlUpdateTest.getJsonObject("request"), expectedResponse);

    Exception e = null;
    try {
      Thread.sleep(2_000);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }

    String testContainerHost = System.getProperty("cassandra.tenant." + "default-tenant" + ".host");
    String testContainerPort = System.getProperty("cassandra.tenant." + "default-tenant" + ".port");

    List<Integer> ttls = new ArrayList<>();
    try (CqlSession session =
        CqlSession.builder()
            .withLocalDatacenter("datacenter1")
            .addContactPoint(
                new InetSocketAddress(testContainerHost, Integer.parseInt(testContainerPort)))
            .build()) {
      ResultSet resultSet =
          session.execute(
              "SELECT default_time_to_live\n"
                  + "FROM system_schema.tables\n"
                  + "WHERE keyspace_name = ? AND table_name = ?;",
              "ofs",
              "t15");

      resultSet.forEach(r -> ttls.add(r.getInt(0)));
    } catch (Exception exception) {
      e = exception;
    }
    // assert no exception
    assert e == null;

    assert ttls.size() == 1;
    assert ttls.get(0) == 60000;
  }
}
