package com.qingyun.systemconfig.repository;

import com.qingyun.systemconfig.model.SystemConfigItem;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SystemConfigRepository {

    private static final RowMapper<SystemConfigItem> ROW_MAPPER = (resultSet, rowNum) -> {
        SystemConfigItem item = new SystemConfigItem();
        item.setId(resultSet.getLong("id"));
        item.setConfigKey(resultSet.getString("config_key"));
        item.setConfigName(resultSet.getString("config_name"));
        item.setConfigGroup(resultSet.getString("config_group"));
        item.setConfigValue(resultSet.getString("config_value"));
        item.setValueType(resultSet.getString("value_type"));
        item.setDescription(resultSet.getString("description"));
        item.setEnabled(resultSet.getBoolean("is_enabled"));
        item.setCreatedAt(resultSet.getObject("created_at", LocalDateTime.class));
        item.setUpdatedAt(resultSet.getObject("updated_at", LocalDateTime.class));
        return item;
    };

    private final JdbcTemplate jdbcTemplate;

    public SystemConfigRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SystemConfigItem> findAll() {
        String sql = """
                select id, config_key, config_name, config_group, config_value, value_type,
                       description, is_enabled, created_at, updated_at
                from system_config
                order by config_group asc, config_key asc
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public boolean existsByConfigKey(String configKey) {
        String sql = "select count(1) from system_config where config_key = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, configKey);
        return count != null && count > 0;
    }

    public boolean existsByConfigKeyExcludingId(String configKey, Long id) {
        String sql = "select count(1) from system_config where config_key = ? and id <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, configKey, id);
        return count != null && count > 0;
    }

    public void insert(SystemConfigItem item) {
        String sql = """
                insert into system_config(config_key, config_name, config_group, config_value, value_type,
                                        description, is_enabled, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql,
                item.getConfigKey(),
                item.getConfigName(),
                item.getConfigGroup(),
                item.getConfigValue(),
                item.getValueType(),
                item.getDescription(),
                item.isEnabled(),
                item.getCreatedAt(),
                item.getUpdatedAt());
    }

    public void update(Long id, SystemConfigItem item) {
        String sql = """
                update system_config
                set config_key = ?,
                    config_name = ?,
                    config_group = ?,
                    config_value = ?,
                    value_type = ?,
                    description = ?,
                    is_enabled = ?,
                    updated_at = ?
                where id = ?
                """;
        jdbcTemplate.update(sql,
                item.getConfigKey(),
                item.getConfigName(),
                item.getConfigGroup(),
                item.getConfigValue(),
                item.getValueType(),
                item.getDescription(),
                item.isEnabled(),
                item.getUpdatedAt(),
                id);
    }

    public void updateEnabled(Long id, boolean enabled, LocalDateTime updatedAt) {
        String sql = "update system_config set is_enabled = ?, updated_at = ? where id = ?";
        jdbcTemplate.update(sql, enabled, updatedAt, id);
    }
}