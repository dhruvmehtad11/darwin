package com.dream11.admin.constants;

public class ExpectedDataPath {
  private static final String WorkDir =
      System.getProperty("user.dir") == null ? "" : System.getProperty("user.dir") + "/";
  public static String CassandraEntityTestsDataPath =
      WorkDir + "src/test/resources/testdata/CassandraEntityTests.json";
  public static String CassandraFeatureGroupTestsDataPath =
      WorkDir + "src/test/resources/testdata/CassandraFeatureGroupTests.json";
  public static String CassandraEntityTtlTestsDataPath =
      WorkDir + "src/test/resources/testdata/CassandraEntityTtlTests.json";
  public static String CassandraMetastoreSearchTestsDataPath =
      WorkDir + "src/test/resources/testdata/CassandraMetastoreCacheWarmingRestTests.json";

  public static String ConsumerAdminTestDataPath =
      WorkDir + "src/test/resources/testdata/ConsumerAdminTests.json";
  
  public static String FctAppLayerTestsDataPath =
      WorkDir + "src/test/resources/testdata/FctAppLayerTests.json";

  public static String S3MetastoreTestsDataPath =
      WorkDir + "src/test/resources/testdata/S3MetastoreTests.json";

  public static String KafkaTopicMetadataTestsDataPath =
      WorkDir + "src/test/resources/testdata/KafkaTopicMetadataTests.json";
}
