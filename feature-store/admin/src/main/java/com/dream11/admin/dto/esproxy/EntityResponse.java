package com.dream11.admin.dto.esproxy;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntityResponse {
    private String entityName;

    private List<String> type;

    private List<String> joinKeys;

    private String description;

    public static EntityResponse getResponse(GetEntityResponse res){
      return EntityResponse.builder()
          .entityName(res.getEntity().getName())
          .joinKeys(res.getEntity().getJoinKeys())
          .type(res.getEntity().getValueType())
          .description(res.getEntity().getDescription())
          .build();
    }

}
