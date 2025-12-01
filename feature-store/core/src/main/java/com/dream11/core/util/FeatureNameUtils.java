package com.dream11.core.util;

import com.dream11.core.dto.entity.CassandraEntity;
import com.dream11.core.dto.featurecolumn.CassandraFeatureColumn;

import java.util.HashSet;
import java.util.Set;

public class FeatureNameUtils {
  public static final String FG_COL_NAME_SEPARATOR = "__";

  public static String getFgColumnName(
      String featureGroupName, String version, String columnName, Boolean versionEnabled) {
    if (!versionEnabled) {
      return featureGroupName + FG_COL_NAME_SEPARATOR + columnName;
    }

    return featureGroupName + FG_COL_NAME_SEPARATOR + version + FG_COL_NAME_SEPARATOR + columnName;
  }

  public static String getFgColumnName(String featureGroupName, String version, String columnName) {
    return featureGroupName + FG_COL_NAME_SEPARATOR + version + FG_COL_NAME_SEPARATOR + columnName;
  }

  public static String getColumnNameFromFgColumn(String featureGroupColumnName) {
    int separatorIndex = featureGroupColumnName.lastIndexOf(FG_COL_NAME_SEPARATOR);
    if (separatorIndex != -1) {
      return featureGroupColumnName.substring(separatorIndex + FG_COL_NAME_SEPARATOR.length());
    } else {
      // Handle the case where the separator is not found
      throw new IllegalArgumentException(
          "Invalid feature group column name: " + featureGroupColumnName);
    }
  }

  public static String getFgNameFromColumn(String featureGroupColumnName) {
    int separatorIndex = featureGroupColumnName.lastIndexOf(FG_COL_NAME_SEPARATOR);
    if (separatorIndex != -1) {
      return featureGroupColumnName.substring(0, separatorIndex);
    } else {
      // Handle the case where the separator is not found
      throw new IllegalArgumentException(
          "Invalid feature group column name: " + featureGroupColumnName);
    }
  }

  public static Boolean isFeatureNameValid(String featureName) {
    return !featureName.contains(FG_COL_NAME_SEPARATOR);
  }

  public static Set<String> getCassandraEntityFeatureSet(CassandraEntity cassandraEntity, Boolean lowercaseSchema) {
    Set<String> featureNames = new HashSet<>();
    for (CassandraFeatureColumn col : cassandraEntity.getFeatures()) {
      if (lowercaseSchema) {
        featureNames.add(col.getFeatureName().toLowerCase());
      } else {
        featureNames.add(col.getFeatureName());
      }
    }
    return featureNames;
  }
}
