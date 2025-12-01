package com.dream11.core.dto.request.legacystack;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LegacyBulkReadFeaturesRequest implements LegacyRequest {
  @NotNull
  @JsonProperty("TableName")
  private String tableName;

  @NotNull
  @JsonProperty("PrimaryKeys")
  private Map<String, Object> primaryKeyValues;

  @NotNull
  @JsonProperty("InValArray")
  private LegacyInValArray inValArray;

  @NotNull
  @Valid
  @JsonProperty("Columns")
  private List<LegacyReadFeatureColumn> readFeatureColumns;

  public void patchRequest(){
    tableName = tableName.toLowerCase();

    Map<String, Object> map = new HashMap<>();
    for(String key: primaryKeyValues.keySet()){
      map.put(key.toLowerCase(), primaryKeyValues.get(key));
    }
    primaryKeyValues = map;

    inValArray.patchRequest();
    for(LegacyReadFeatureColumn column:readFeatureColumns){
      column.patchRequest();
    }
  }
}
