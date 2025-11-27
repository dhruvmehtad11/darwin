package com.dream11.populator.expectedrequestresponse;

import static com.dream11.core.util.Utils.getFileBuffer;
import static com.dream11.populator.constants.ExpectedDataPath.PopulatorWriterDataPath;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PopulatorWriterTest {
  private static final JsonObject PopulatorWriterTestData =
      new JsonObject(getFileBuffer(PopulatorWriterDataPath));

  public static final JsonObject SuccessWriterTest = PopulatorWriterTestData.getJsonObject("successWriterTest");
}
