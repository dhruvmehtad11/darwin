package com.dream11.core.dto.request.legacystack;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LegacyWriteFeaturesKafkaMessage implements LegacyRequest {
  // dummy message
  // {"Features":"{\"userid\":502,\"id\":15,\"name\":\"A7Hxm8JX\",\"col1\":5161,\"col2\":\"Tp2FL\",\"col3\":36,\"col6\":false}",
  // "EntityName":"fg_test_entity","Entities":"userid,id,name","FeatureGroup":"fg_test"}
  //                                ^^^^ not list as expected by legacy write api
  public static final LegacyWriteFeaturesKafkaMessage EMPTY_REQUEST =
      LegacyWriteFeaturesKafkaMessage.builder().build();

  @NotNull
  @JsonProperty("EntityName")
  private String entityName;

  @NotNull
  @JsonProperty("Entities")
  private String entityColumns;

  @NotNull
  @JsonProperty("FeatureGroup")
  private String featureGroupName;

  @NotNull
  @JsonProperty("Features")
  private String writeFeatures;

  public static LegacyWriteFeaturesRequest getWriteFeaturesRequest(
      ObjectMapper objectMapper, LegacyWriteFeaturesKafkaMessage legacyWriteFeaturesKafkaMessage)
      throws JsonProcessingException {
    return LegacyWriteFeaturesRequest.builder()
        .entityName(legacyWriteFeaturesKafkaMessage.entityName)
        .entityColumns(
            new ArrayList<>(
                Arrays.asList(
                    legacyWriteFeaturesKafkaMessage.entityColumns.split(
                        ",")))) // comma separated list
        .featureGroupName(legacyWriteFeaturesKafkaMessage.featureGroupName)
        .writeFeatures(
            objectMapper.readValue(
                legacyWriteFeaturesKafkaMessage.writeFeatures, new TypeReference<>() {}))
        .build();
  }

  public static LegacyWriteFeaturesKafkaMessage create(
      ObjectMapper objectMapper, LegacyWriteFeaturesRequest legacyWriteFeaturesRequest)
      throws JsonProcessingException {
    StringBuilder entities = new StringBuilder();
    for (String entity : legacyWriteFeaturesRequest.getEntityColumns()) {
      entities.append(entity);
      if (entities.indexOf(entity) != entities.length() - 1) entities.append(",");
    }

    return LegacyWriteFeaturesKafkaMessage.builder()
        .entityName(legacyWriteFeaturesRequest.getEntityName())
        .entityColumns(entities.toString())
        .featureGroupName(legacyWriteFeaturesRequest.getFeatureGroupName())
        .writeFeatures(
            objectMapper.writeValueAsString(legacyWriteFeaturesRequest.getWriteFeatures()))
        .build();
  }
}
