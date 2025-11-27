package com.dream11.core.dto.request.legacystack;

import com.dream11.core.dto.entity.CassandraEntity;
import com.dream11.core.dto.featurecolumn.CassandraFeatureColumn;
import com.dream11.core.dto.request.CreateCassandraEntityRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.dream11.core.constant.Constants.*;
import static com.dream11.core.util.LegacyStackUtils.patchLegacyColumnType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LegacyCreateTableRequest implements LegacyRequest{
  @NotNull
  @JsonProperty("Name")
  private String tableName;

  @NotNull
  @JsonProperty("Columns")
  private Map<String, String> featureColumns;

  @NotNull
  @JsonProperty("PrimaryKeys")
  private List<String> primaryKeys;

  @JsonProperty(value = "Ttl", defaultValue = "0")
  private Long ttl = 0L;

  public static List<CassandraFeatureColumn> getCassandraEntityFeatures(
      LegacyCreateTableRequest legacyCreateTableRequest) {
    List<CassandraFeatureColumn> res = new ArrayList<>();
    for (Map.Entry<String, String> entry :
        legacyCreateTableRequest.getFeatureColumns().entrySet()) {
      CassandraFeatureColumn.CassandraDataType cassandraDataType;
      try {
        String colType = patchLegacyColumnType(entry.getValue());
        cassandraDataType =
            CassandraFeatureColumn.CassandraDataType.valueOf(colType.toUpperCase());
      } catch (Exception e) {
        throw new RuntimeException(INVALID_DATA_TYPE_ERROR_MESSAGE + entry.getValue());
      }
      res.add(
          CassandraFeatureColumn.builder()
              .featureName(entry.getKey())
              .featureDataType(cassandraDataType)
              .build());
    }
    return res;
  }

  public static CreateCassandraEntityRequest getCreateCassandraEntityRequest(
      LegacyCreateTableRequest legacyCreateTableRequest) {
    return CreateCassandraEntityRequest.builder()
        .entity(
            CassandraEntity.builder()
                .tableName(legacyCreateTableRequest.getTableName())
                .features(
                    LegacyCreateTableRequest.getCassandraEntityFeatures(
                        legacyCreateTableRequest))
                .primaryKeys(legacyCreateTableRequest.getPrimaryKeys())
                .ttl(legacyCreateTableRequest.getTtl())
                .build())
        .owner(OLD_SDK_OWNER)
        .description("")
        .tags(EMPTY_TAG_ARRAY)
        .build();
  }
}