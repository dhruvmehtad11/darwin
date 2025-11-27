package com.dream11.spark.constants;


public class ExpectedDataPath {
  private static final String WorkDir =
      System.getProperty("user.dir") == null ? "" : System.getProperty("user.dir") + "/";
  public static String OfsMockDataPath =
      WorkDir + "src/test/resources/mockserver/OfsMockData.json";

  public static String OfsTestDataPath =
      WorkDir + "src/test/resources/testdata/WriterTest.json";
}
