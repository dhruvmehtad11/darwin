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
public class LegacyReadFeaturesRequest implements LegacyRequest {
  @NotNull
  @JsonProperty("TableName")
  private String tableName;

  @NotNull
  @JsonProperty("Values")
  private Map<String, Object> primaryKeyValues;

  @NotNull
  @Valid
  @JsonProperty("Columns")
  private List<LegacyReadFeatureColumn> readFeatureColumns;

  // converting to lowercase to support legacy stack
  public void patchRequest(){
    tableName = tableName.toLowerCase();

    Map<String, Object> map = new HashMap<>();
    for(String key: primaryKeyValues.keySet()){
      map.put(key.toLowerCase(), primaryKeyValues.get(key));
    }
    primaryKeyValues = map;

    for(LegacyReadFeatureColumn column:readFeatureColumns){
      column.patchRequest();
    }
  }
}
