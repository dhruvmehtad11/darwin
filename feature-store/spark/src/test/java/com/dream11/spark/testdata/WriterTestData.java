package com.dream11.spark.testdata;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static com.dream11.spark.MockServers.OfsMock.getFileBuffer;
import static com.dream11.spark.constants.ExpectedDataPath.OfsTestDataPath;

public class WriterTestData {
  public static final JsonObject testData = JsonParser.parseString(getFileBuffer(OfsTestDataPath)).getAsJsonObject();
  public static final JsonObject successWriteTest = testData.getAsJsonObject("successWriteTest");
  public static final JsonObject successWriteWithNullNonPrimaryKey = testData.getAsJsonObject("successWriteWithNullNonPrimaryKey");

}
