package com.dream11.admin.expectedrequestresponse;

import static com.dream11.admin.constants.ExpectedDataPath.CassandraEntityTestsDataPath;
import static com.dream11.core.util.Utils.getFileBuffer;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CassandraEntityTestData {
  private static final JsonObject CassandraEntityTests =
      new JsonObject(getFileBuffer(CassandraEntityTestsDataPath));

  public static final JsonObject SuccessEntityCreationTest = CassandraEntityTests.getJsonObject("successEntityCreationTest");
  public static final JsonObject InvalidRequestEntityCreationTest = CassandraEntityTests.getJsonObject("invalidRequestEntityCreationTest");
  public static final JsonObject InvalidRequestEntityCreationTest1 = CassandraEntityTests.getJsonObject("invalidRequestEntityCreationTest1");
  public static final JsonObject EntityAlreadyExistsTest = CassandraEntityTests.getJsonObject("entityAlreadyExistsTest");
  public static final JsonObject SuccessGetEntityTest = CassandraEntityTests.getJsonObject("successGetEntityTest");
  public static final JsonObject SuccessGetEntityMetadataTest = CassandraEntityTests.getJsonObject("successGetEntityMetadataTest");
  public static final JsonObject FailedGetEntityTest = CassandraEntityTests.getJsonObject("failedGetEntityTest");
  public static final JsonObject InvalidRequestGetEntityTest = CassandraEntityTests.getJsonObject("invalidRequestGetEntityTest");
  public static final JsonObject CreateEntityInvalidSchemaTest1 = CassandraEntityTests.getJsonObject("createEntityInvalidSchemaTest1");
  public static final JsonObject CreateEntityInvalidSchemaTest2 = CassandraEntityTests.getJsonObject("createEntityInvalidSchemaTest2");


}
