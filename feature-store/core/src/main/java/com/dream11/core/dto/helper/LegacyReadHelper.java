package com.dream11.core.dto.helper;

import com.dream11.core.dto.featuredata.CassandraFeatureData;
import com.dream11.core.dto.request.ReadCassandraFeaturesRequest;
import com.dream11.core.dto.request.legacystack.LegacyBulkReadFeaturesRequest;
import com.dream11.core.dto.request.legacystack.LegacyReadFeatureColumn;
import com.dream11.core.dto.request.legacystack.LegacyReadFeaturesRequest;
import com.dream11.core.dto.response.ReadCassandraFeaturesResponse;

import java.util.*;
import com.dream11.core.error.ApiRestException;
import com.dream11.core.error.ServiceError;
import lombok.extern.slf4j.Slf4j;

import static com.dream11.core.constant.Constants.*;
import static com.dream11.core.util.FeatureNameUtils.*;

@lombok.Data
@Slf4j
public class LegacyReadHelper {
  private String featureGroupName;
  private Map<String, String> featureColumns;
  private CassandraFeatureData primaryKeys;

  public LegacyReadHelper(LegacyBulkReadFeaturesRequest legacyBulkReadFeaturesRequest) {
    Map<String, String> columns = new HashMap<>();
    String fgName = null;
    for (LegacyReadFeatureColumn col : legacyBulkReadFeaturesRequest.getReadFeatureColumns()) {
      if (col.getName().contains(FG_COL_NAME_SEPARATOR)) {
        if (fgName == null) {
          fgName = getFgNameFromColumn(col.getName());
        } else if (!fgName.equals(getFgNameFromColumn(col.getName()))) {
          throw new RuntimeException(INVALID_REQUEST_ERROR_MESSAGE);
        }

        if (columns.get(getColumnNameFromFgColumn(col.getName())) != null) {
          throw new RuntimeException(DUPLICATE_COLUMNS_ERROR_MESSAGE);
        }

        columns.put(getColumnNameFromFgColumn(col.getName()), col.getResponseColumnName());
      } else {
        columns.put(col.getName(), col.getResponseColumnName());
      }
    }

    List<String> pkNames =
        new ArrayList<>(legacyBulkReadFeaturesRequest.getPrimaryKeyValues().keySet());
    List<List<Object>> pkValues = new ArrayList<>();
    pkNames.add(legacyBulkReadFeaturesRequest.getInValArray().getColumnName());
    for (Object o : legacyBulkReadFeaturesRequest.getInValArray().getValues()) {
      List<Object> pkVal =
          new ArrayList<>(legacyBulkReadFeaturesRequest.getPrimaryKeyValues().values());
      pkVal.add(o);
      pkValues.add(pkVal);
    }

    this.featureGroupName = fgName;
    this.featureColumns = columns;
    this.primaryKeys = CassandraFeatureData.builder().names(pkNames).values(pkValues).build();
  }

  public LegacyReadHelper(LegacyReadFeaturesRequest legacyReadFeaturesRequest) {
    Map<String, String> columns = new HashMap<>();
    String fgName = null;
    for (LegacyReadFeatureColumn col : legacyReadFeaturesRequest.getReadFeatureColumns()) {
      if (col.getName().contains(FG_COL_NAME_SEPARATOR)) {
        if (fgName == null) {
          fgName = getFgNameFromColumn(col.getName());
        } else if (!fgName.equals(getFgNameFromColumn(col.getName()))) {
          throw new ApiRestException(INVALID_REQUEST_ERROR_MESSAGE, ServiceError.READ_FEATURE_GROUP_INVALID_REQUEST_EXCEPTION);
        }

        if (columns.get(getColumnNameFromFgColumn(col.getName())) != null) {
          throw new ApiRestException(DUPLICATE_COLUMNS_ERROR_MESSAGE, ServiceError.READ_FEATURE_GROUP_INVALID_REQUEST_EXCEPTION);
        }

        columns.put(getColumnNameFromFgColumn(col.getName()), col.getResponseColumnName());
      } else {
        columns.put(col.getName(), col.getResponseColumnName());
      }
    }

    // if fg name could not be resolved from any feature columns
    if(fgName == null){
      throw new ApiRestException(INVALID_REQUEST_ERROR_MESSAGE, ServiceError.READ_FEATURE_GROUP_INVALID_REQUEST_EXCEPTION);
    }

    this.featureGroupName = fgName;
    this.featureColumns = columns;
    this.primaryKeys =
        CassandraFeatureData.builder()
            .names(new ArrayList<>(legacyReadFeaturesRequest.getPrimaryKeyValues().keySet()))
            .values(
                List.of(new ArrayList<>(legacyReadFeaturesRequest.getPrimaryKeyValues().values())))
            .build();
  }

  public static ReadCassandraFeaturesRequest getReadRequest(LegacyReadHelper legacyReadHelper) {
    List<String> featureColumns = new ArrayList<>(legacyReadHelper.getFeatureColumns().keySet());

    return ReadCassandraFeaturesRequest.builder()
        .featureGroupName(legacyReadHelper.getFeatureGroupName())
        .featureColumns(featureColumns)
        .primaryKeys(legacyReadHelper.primaryKeys)
        .build();
  }

  public static Map<String, Object> getResponseMap(
      LegacyReadHelper legacyReadHelper, ReadCassandraFeaturesResponse response) {
    Map<String, Object> res = new HashMap<>();
    List<Object> featureVector;
    try {
      featureVector = response.getSuccessfulKeys().get(0).getFeatures();
    } catch (Exception e) {
      throw new RuntimeException(EMPTY_RESPONSE_ERROR_STRING);
    }
    int i = 0;
    for (String key : legacyReadHelper.getFeatureColumns().keySet()) {
      res.put(legacyReadHelper.getFeatureColumns().get(key), featureVector.get(i++));
    }
    return res;
  }

  public static List<Map<String, Object>> getBulkResponseMap(
      LegacyReadHelper legacyReadHelper, ReadCassandraFeaturesResponse response) {
    List<Map<String, Object>> res = new ArrayList<>();
    for (int i = 0; i < response.getSuccessfulKeys().size(); i++) {
      int j = 0;
      Map<String, Object> features = new HashMap<>();
      for (String key : legacyReadHelper.getFeatureColumns().keySet()) {
        features.put(legacyReadHelper.getFeatureColumns().get(key), response.getSuccessfulKeys().get(i).getFeatures().get(j++));
      }
      res.add(features);
    }
    return res;
  }
}
