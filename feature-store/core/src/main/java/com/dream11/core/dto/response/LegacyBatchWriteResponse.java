package com.dream11.core.dto.response;

import com.dream11.core.dto.helper.LegacyBatchWriteFailedRecords;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LegacyBatchWriteResponse {
  @JsonProperty("Success")
  private Boolean success;

  @JsonProperty("Message")
  private String message;

  @JsonProperty("Successful_records")
  private Integer successfulRecords;

  @JsonProperty("Failed_records")
  private List<LegacyBatchWriteFailedRecords> failedRecords;
}
