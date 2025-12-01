package com.dream11.core.dto.request.legacystack;

import com.dream11.core.dto.featuredata.CassandraFeatureData;
import com.dream11.core.dto.request.WriteCassandraFeaturesRequest;
import com.dream11.core.dto.request.interfaces.WriteFeaturesRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LegacyWriteFeaturesRequest implements LegacyRequest {
  public static final LegacyWriteFeaturesRequest EMPTY_REQUEST = LegacyWriteFeaturesRequest.builder().build();

  @NotNull
  @JsonProperty("EntityName")
  private String entityName;

  @NotNull
  @JsonProperty("Entities")
  private List<String> entityColumns;

  @NotNull
  @JsonProperty("FeatureGroup")
  private String featureGroupName;

  @NotNull
  @JsonProperty("Features")
  private Map<String, Object> writeFeatures;

  public static WriteCassandraFeaturesRequest getWriteFeaturesRequest(
      LegacyWriteFeaturesRequest legacyWriteFeaturesRequest) {
    List<String> names = new ArrayList<>();
    List<Object> values = new ArrayList<>();
    for (Map.Entry<String, Object> e : legacyWriteFeaturesRequest.getWriteFeatures().entrySet()) {
      names.add(e.getKey());
      values.add(e.getValue());
    }

    return WriteCassandraFeaturesRequest.builder()
        .featureGroupName(legacyWriteFeaturesRequest.getFeatureGroupName())
        .features(CassandraFeatureData.builder().names(names).values(List.of(values)).build())
        .build();
  }

  // converting to lowercase to support legacy stack
  public void patchRequest(){
    entityName = entityName.toLowerCase();
    featureGroupName = featureGroupName.toLowerCase();
    List<String> lowerEntityCols = new ArrayList<>();
    for(String entityCol : entityColumns){
      lowerEntityCols.add(entityCol.toLowerCase());
    }
    entityColumns = lowerEntityCols;

    Map<String, Object> map = new HashMap<>();
    for(String key: writeFeatures.keySet()){
      map.put(key.toLowerCase(), writeFeatures.get(key));
    }
    writeFeatures = map;
  }
}
