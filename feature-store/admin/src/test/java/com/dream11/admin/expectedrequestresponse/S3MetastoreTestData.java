package com.dream11.admin.expectedrequestresponse;

import static com.dream11.admin.constants.ExpectedDataPath.S3MetastoreTestsDataPath;
import static com.dream11.core.util.Utils.getFileBuffer;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S3MetastoreTestData {
  private static final JsonObject S3MetastoreTests =
      new JsonObject(getFileBuffer(S3MetastoreTestsDataPath));

  public static final JsonObject S3backupSuccessTest = S3MetastoreTests.getJsonObject("s3backupSuccessTest");
}
