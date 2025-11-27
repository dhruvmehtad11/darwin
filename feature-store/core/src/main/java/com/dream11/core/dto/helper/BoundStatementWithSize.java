package com.dream11.core.dto.helper;

import java.util.List;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BoundStatementWithSize {
  private BoundStatement statement;
  private Long payloadSize;
}
