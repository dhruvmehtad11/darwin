package com.dream11.populator.expectedtestdata;

import com.dream11.populator.constants.ExpectedDataPath;
import io.vertx.core.json.JsonObject;

import static com.dream11.core.util.Utils.getFileBuffer;

public class ExpectedData {
  public static final JsonObject testdata = new JsonObject(getFileBuffer(ExpectedDataPath.ExpectedDataPath));
  public static final JsonObject populatorFinalConfig = testdata.getJsonObject("populatorMetadata");
}
