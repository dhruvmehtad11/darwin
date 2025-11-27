package com.dream11.core.util;

public class CassandraStatementCacheUtils {
  private static final String NAME_SEPARATOR = "__";
  private static final String METADATA = "METADATA";

//  public static WriteCassandraFeatureGroupCacheKey getWriteQueryKey(
//      String featureGroupName, String featureGroupVersion) {
//    return WriteCassandraFeatureGroupCacheKey.builder()
//        .featureGroupName(featureGroupName)
//        .featureGroupVersion(featureGroupVersion)
//        .build();
//    //    return featureGroupName + NAME_SEPARATOR + featureGroupVersion;
//  }
//
//  public static ReadCassandraFeatureGroupCacheKey getReadQueryKey(
//      String featureGroupName,
//      String featureGroupVersion,
//      List<String> primaryKeys,
//      List<String> columns) {
//    return ReadCassandraFeatureGroupCacheKey.builder()
//        .featureGroupName(featureGroupName)
//        .featureGroupVersion(featureGroupVersion)
//        .primaryKeys(primaryKeys)
//        .columns(columns)
//        .build();
//    //    StringBuilder key = new StringBuilder(featureGroupName);
//    //    key.append(NAME_SEPARATOR);
//    //    key.append(featureGroupVersion);
//    //    key.append(NAME_SEPARATOR);
//    //
//    //    int pkSize = primaryKeys.size();
//    //    for (int i = 0; i < pkSize; i++) {
//    //      key.append(primaryKeys.get(i));
//    //      if (i < pkSize - 1) key.append(NAME_SEPARATOR);
//    //    }
//    //
//    //    int colSize = columns.size();
//    //    for (int i = 0; i < colSize; i++) {
//    //      key.append(columns.get(i));
//    //      if (i < colSize - 1) key.append(NAME_SEPARATOR);
//    //    }
//    //    return key.toString();
//  }

  public static String getFgMetaDataKey(String featureGroupName, String featureGroupVersion) {
    return METADATA + NAME_SEPARATOR + featureGroupName + NAME_SEPARATOR + featureGroupVersion;
  }

  public static String getEntityMetaDataKey(String entityName) {
    return METADATA + NAME_SEPARATOR + entityName;
  }
}
