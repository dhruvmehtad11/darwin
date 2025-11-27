package com.dream11.core.dto.request.legacystack;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LegacyMultiBulkReadFeaturesRequest implements LegacyRequest {
  @NotNull
  @Size(max = 10, message = "max 10 batches allowed per request")
  @JsonProperty("Batches")
  private List<LegacyReadFeaturesRequest> Batches;
}
