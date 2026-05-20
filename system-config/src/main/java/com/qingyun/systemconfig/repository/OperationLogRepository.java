package com.qingyun.systemconfig.repository;

import com.qingyun.systemconfig.model.OperationLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class OperationLogRepository {

    private static final RowMapper<OperationLog> ROW_MAPPER = (resultSet, rowNum) -> {
        OperationLog operationLog = new OperationLog();
        operationLog.setId(resultSet.getLong("id"));
        operationLog.setOperatorId(resultSet.getObject("operator_id", Long.class));
        operationLog.setModuleName(resultSet.getString("module_name"));
        operationLog.setOperationType(resultSet.getString("operation_type"));
        operationLog.setOperationContent(resultSet.getString("operation_content"));
        operationLog.setCreatedAt(resultSet.getObject("created_at", LocalDateTime.class));
        return operationLog;
    };

    private final JdbcTemplate jdbcTemplate;

    public OperationLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<OperationLog> findRecent(int limit) {
        String sql = """
                select id, operator_id, module_name, operation_type, operation_content, created_at
                from operation_log
                order by created_at desc
                limit ?
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, limit);
    }

    public void insert(Long operatorId, String moduleName, String operationType, String operationContent,
            LocalDateTime now) {
        String sql = """
                insert into operation_log(operator_id, module_name, operation_type, operation_content, created_at)
                values (?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql, operatorId, moduleName, operationType, operationContent, now);
    }
}