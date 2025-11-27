package com.dream11.admin.expectedrequestresponse;

import static com.dream11.admin.constants.ExpectedDataPath.CassandraFeatureGroupTestsDataPath;
import static com.dream11.admin.constants.ExpectedDataPath.CassandraMetastoreSearchTestsDataPath;
import static com.dream11.core.util.Utils.getFileBuffer;

import io.vertx.core.json.JsonObject;

public class CassandraSearchTestData {
  private static final JsonObject CassandraMetastoreSearchTests =
      new JsonObject(getFileBuffer(CassandraMetastoreSearchTestsDataPath));

  public static final JsonObject GetAllEntitiesTest =  CassandraMetastoreSearchTests.getJsonObject("getAllEntitiesTest");
  public static final JsonObject GetAllUpdatedEntitiesTest =  CassandraMetastoreSearchTests.getJsonObject("getAllUpdatedEntitiesTest");
  public static final JsonObject GetAllFeatureGroupsTest =  CassandraMetastoreSearchTests.getJsonObject("getAllFeatureGroupsTest");
  public static final JsonObject GetAllUpdatedFeatureGroupsTest =  CassandraMetastoreSearchTests.getJsonObject("getAllUpdatedFeatureGroupsTest");
  public static final JsonObject GetAllFeatureGroupsLatestVersionTest =  CassandraMetastoreSearchTests.getJsonObject("getAllFeatureGroupsLatestVersionTest");
  public static final JsonObject GetAllUpdatedFeatureGroupsLatestVersionTest =  CassandraMetastoreSearchTests.getJsonObject("getAllUpdatedFeatureGroupsLatestVersionTest");
}
