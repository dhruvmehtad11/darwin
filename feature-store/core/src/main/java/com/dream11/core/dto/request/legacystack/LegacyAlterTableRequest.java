package com.dream11.core.dto.request.legacystack;

import com.dream11.core.dto.entity.CassandraEntity;
import com.dream11.core.dto.featurecolumn.CassandraFeatureColumn;
import com.dream11.core.dto.featuregroup.CassandraFeatureGroup;
import com.dream11.core.dto.request.CreateCassandraEntityRequest;
import com.dream11.core.dto.request.CreateCassandraFeatureGroupRequest;
import com.dream11.core.error.ApiRestException;
import com.dream11.core.error.ServiceError;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.reactivex.Single;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.dream11.core.constant.Constants.*;
import static com.dream11.core.constant.Constants.EMPTY_TAG_ARRAY;
import static com.dream11.core.util.FeatureNameUtils.*;
import static com.dream11.core.util.LegacyStackUtils.patchLegacyColumnType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LegacyAlterTableRequest implements LegacyRequest {
  @NotNull
  @JsonProperty("Name")
  private String tableName;

  @NotNull
  @JsonProperty("Columns")
  private Map<String, String> featureColumns;

  @JsonIgnore
  private String fgName;

  public static Boolean validate(LegacyAlterTableRequest legacyAlterTableRequest) {
    String fgName = null;
    for (String col : legacyAlterTableRequest.getFeatureColumns().keySet()) {
      if (!col.contains(FG_COL_NAME_SEPARATOR)) {
        throw new RuntimeException(INVALID_REQUEST_ERROR_MESSAGE);
      }

      if (fgName == null) {
        fgName = getFgNameFromColumn(col);
      } else if (!fgName.equals(getFgNameFromColumn(col))) {
        throw new RuntimeException(INVALID_REQUEST_ERROR_MESSAGE);
      }
    }
    legacyAlterTableRequest.setFgName(fgName);
    return Boolean.TRUE;
  }

  public static List<CassandraFeatureColumn> getCassandraFeatureGroupFeatures(
      LegacyAlterTableRequest legacyAlterTableRequest) {
    List<CassandraFeatureColumn> res = new ArrayList<>();
    for (Map.Entry<String, String> entry : legacyAlterTableRequest.getFeatureColumns().entrySet()) {
      CassandraFeatureColumn.CassandraDataType cassandraDataType;
      try {
        String colType = patchLegacyColumnType(entry.getValue());
        cassandraDataType =
            CassandraFeatureColumn.CassandraDataType.valueOf(colType.toUpperCase());
      } catch (Exception e) {
        throw new ApiRestException(INVALID_DATA_TYPE_ERROR_MESSAGE + entry.getValue(), ServiceError.ENTITY_SCHEMA_VALIDATION_EXCEPTION);
      }

      String colName = getColumnNameFromFgColumn(entry.getKey());
      res.add(
          CassandraFeatureColumn.builder()
              .featureName(colName)
              .featureDataType(cassandraDataType)
              .build());
    }
    return res;
  }

  public static Single<CreateCassandraFeatureGroupRequest> getCreateCassandraFeatureGroupRequest(
      LegacyAlterTableRequest legacyAlterTableRequest) {
    return Single.just(legacyAlterTableRequest)
        .flatMap(ignore -> {
          try {
            return Single.just(CreateCassandraFeatureGroupRequest.builder()
                .featureGroup(
                    CassandraFeatureGroup.builder()
                        .featureGroupName(legacyAlterTableRequest.getFgName())
                        .versionEnabled(Boolean.FALSE)
                        .featureGroupType(CassandraFeatureGroup.FeatureGroupType.ONLINE)
                        .entityName(legacyAlterTableRequest.getTableName())
                        .features(
                            LegacyAlterTableRequest.getCassandraFeatureGroupFeatures(
                                legacyAlterTableRequest))
                        .build())
                .owner(OLD_SDK_OWNER)
                .description("")
                .tags(EMPTY_TAG_ARRAY)
                .build());
          } catch (Exception e) {
            if(e instanceof ApiRestException){
              return Single.error((ApiRestException)e);
            }
            return Single.error(e);
          }
        });
  }
}
