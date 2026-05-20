package com.qingyun.systemconfig.repository;

import com.qingyun.systemconfig.model.SysUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SysUserRepository {

    private static final RowMapper<SysUser> ROW_MAPPER = (resultSet, rowNum) -> {
        SysUser sysUser = new SysUser();
        sysUser.setId(resultSet.getLong("id"));
        sysUser.setUsername(resultSet.getString("username"));
        sysUser.setPassword(resultSet.getString("password"));
        sysUser.setRoleCode(resultSet.getString("role_code"));
        sysUser.setStatus(resultSet.getShort("status"));
        sysUser.setLastLoginTime(resultSet.getObject("last_login_time", LocalDateTime.class));
        sysUser.setCreatedAt(resultSet.getObject("created_at", LocalDateTime.class));
        sysUser.setUpdatedAt(resultSet.getObject("updated_at", LocalDateTime.class));
        return sysUser;
    };

    private final JdbcTemplate jdbcTemplate;

    public SysUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<SysUser> findByUsername(String username) {
        String sql = """
                select id, username, password, role_code, status, last_login_time, created_at, updated_at
                from sys_user
                where username = ?
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, username).stream().findFirst();
    }

    public List<SysUser> findAll() {
        String sql = """
                select id, username, password, role_code, status, last_login_time, created_at, updated_at
                from sys_user
                order by created_at desc
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public boolean existsByUsername(String username) {
        String sql = "select count(1) from sys_user where username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    public void insert(String username,
            String password,
            String roleCode,
            short status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        String sql = """
                insert into sys_user(username, password, role_code, status, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql, username, password, roleCode, status, createdAt, updatedAt);
    }

    public void updateStatus(Long id, short status, LocalDateTime updatedAt) {
        String sql = "update sys_user set status = ?, updated_at = ? where id = ?";
        jdbcTemplate.update(sql, status, updatedAt, id);
    }

    public void updateLastLoginTime(Long id, LocalDateTime lastLoginTime) {
        String sql = "update sys_user set last_login_time = ?, updated_at = ? where id = ?";
        jdbcTemplate.update(sql, lastLoginTime, lastLoginTime, id);
    }
}