package com.dream11.populator.constants;


import io.vertx.ext.web.common.template.test;
import java.awt.*;

public class ExpectedDataPath {
  private static final String WorkDir =
      System.getProperty("user.dir") == null ? "" : System.getProperty("user.dir") + "/";
  public static String OfsMockDataPath =
      WorkDir + "src/test/resources/mockserver/OfsMockData.json";

  public static String ExpectedDataPath =
      WorkDir + "src/test/resources/expectedtestdata/ExpectedData.json";

  public static String PopulatorWriterDataPath =
      WorkDir + "src/test/resources/expectedrequestresponse/PopulatorWriterTestData.json";

  public static String PopulatorWriterTableDataBasePath =
      WorkDir + "src/test/resources/testdeltatables/";
}
