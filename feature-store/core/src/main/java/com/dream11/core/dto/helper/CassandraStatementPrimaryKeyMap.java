package com.dream11.core.dto.helper;

import com.datastax.driver.core.BoundStatement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CassandraStatementPrimaryKeyMap {
    private BoundStatement statement;
    private List<Object> primaryKey;
}
