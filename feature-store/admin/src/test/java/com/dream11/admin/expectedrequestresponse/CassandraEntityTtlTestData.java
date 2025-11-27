package com.dream11.admin.expectedrequestresponse;

import static com.dream11.admin.constants.ExpectedDataPath.CassandraEntityTtlTestsDataPath;
import static com.dream11.core.util.Utils.getFileBuffer;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CassandraEntityTtlTestData {
  private static final JsonObject CassandraEntityTests =
      new JsonObject(getFileBuffer(CassandraEntityTtlTestsDataPath));

  public static final JsonObject SuccessFeatureGroupTtlUpdateTest = CassandraEntityTests.getJsonObject(
      "successFeatureGroupTtlUpdateTest");

}
