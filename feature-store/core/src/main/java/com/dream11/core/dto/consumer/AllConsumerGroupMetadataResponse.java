package com.dream11.core.dto.consumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllConsumerGroupMetadataResponse {
  private List<ConsumerGroupMetadata> consumerGroupMetadata;
}
