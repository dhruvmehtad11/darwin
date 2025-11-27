package com.dream11.core.dto.request.legacystack;

import com.dream11.core.dto.request.WriteCassandraFeaturesBatchRequest;
import com.dream11.core.dto.request.WriteCassandraFeaturesRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LegacyReadFeatureColumn {
  @NotNull
  @JsonProperty("Name")
  private String name;

  @NotNull
  @JsonProperty("ResponseColumnName")
  private String responseColumnName;

  // converting to lowercase to support legacy stack
  public void patchRequest(){
    name = name.toLowerCase();
    responseColumnName = responseColumnName.toLowerCase();
  }
}
