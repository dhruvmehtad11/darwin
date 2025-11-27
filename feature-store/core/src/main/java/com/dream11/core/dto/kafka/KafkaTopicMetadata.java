package com.dream11.core.dto.kafka;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KafkaTopicMetadata {
    private String topicName;
    private Integer partitions;
    private Integer replicationFactor;
    private Long retentionMs;
}