package com.dream11.app.expectedrequestresponse;

import static com.dream11.app.constants.ExpectedDataPath.S3MetastoreTestDataPath;
import static com.dream11.core.util.Utils.getFileBuffer;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S3MetastoreTestData {
  private static final JsonObject S3MetastoreMockData =
      new JsonObject(getFileBuffer(S3MetastoreTestDataPath));

  public static final JsonArray AllEntities =
      S3MetastoreMockData.getJsonArray("entities");
  public static final JsonArray AllFeatureGroups =
      S3MetastoreMockData.getJsonArray("featureGroups");
  public static final JsonArray AllFeatureGroupVersions =
      S3MetastoreMockData.getJsonArray("featureGroupVersions");
}
