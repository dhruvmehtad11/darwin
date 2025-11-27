package com.dream11.core.dto.helper;

import com.datastax.driver.core.BatchStatement;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BatchStatementWithSize {
  private BatchStatement statement;
  private List<Long> payloadSizes;
}
