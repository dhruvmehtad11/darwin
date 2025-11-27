package com.dream11.app.constants;

import com.dream11.app.rest.LegacyStack;

public class ExpectedDataPath {
  private static final String WorkDir =
      System.getProperty("user.dir") == null ? "" : System.getProperty("user.dir") + "/";
  public static String OfsAdminMockDataPath =
      WorkDir + "src/test/resources/mockserver/OfsAdminMockData.json";

  public static String CassandraFeatureGroupWriteTestsDataPath =
      WorkDir + "src/test/resources/testdata/CassandraFeatureGroupWriteTests.json";

  public static String CassandraFeatureGroupReadTestsDataPath =
      WorkDir + "src/test/resources/testdata/CassandraFeatureGroupReadTests.json";

  public static String LegacyStackTestsDataPath =
      WorkDir + "src/test/resources/testdata/LegacyStackTests.json";

  public static String S3MetastoreTestDataPath =
      WorkDir + "src/test/resources/s3metastoretestdata/TestData.json";
}
