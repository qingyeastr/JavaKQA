package com.qingyun.systemconfig.service;

import com.qingyun.framework.security.LoginUser;
import com.qingyun.systemconfig.model.SysUser;
import com.qingyun.systemconfig.repository.SysUserRepository;
import com.qingyun.systemconfig.web.UserForm;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final OperationLogService operationLogService;

    public UserService(SysUserRepository sysUserRepository,
            PasswordEncoder passwordEncoder,
            OperationLogService operationLogService) {
        this.sysUserRepository = sysUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.operationLogService = operationLogService;
    }

    public List<SysUser> listUsers() {
        return sysUserRepository.findAll();
    }

    public void createUser(UserForm userForm, LoginUser operator) {
        validateForm(userForm);
        if (sysUserRepository.existsByUsername(userForm.getUsername())) {
            throw new IllegalArgumentException("用户名已存在，请更换后重试");
        }

        LocalDateTime now = LocalDateTime.now();
        sysUserRepository.insert(
                userForm.getUsername().trim(),
                passwordEncoder.encode(userForm.getPassword().trim()),
                userForm.getRoleCode().trim(),
                userForm.getStatus(),
                now,
                now);
        operationLogService.record(operator.id(), "SYS_USER", "CREATE", "新增管理员账号：" + userForm.getUsername());
    }

    public void updateStatus(Long id, short status, LoginUser operator) {
        sysUserRepository.updateStatus(id, status, LocalDateTime.now());
        operationLogService.record(operator.id(), "SYS_USER", status == 1 ? "ENABLE" : "DISABLE", "切换管理员状态，ID=" + id);
    }

    public void ensureAdminUser() {
        if (sysUserRepository.existsByUsername("admin")) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        sysUserRepository.insert(
                "admin",
                passwordEncoder.encode("123456"),
                "SUPER_ADMIN",
                (short) 1,
                now,
                now);
    }

    private void validateForm(UserForm userForm) {
        if (!StringUtils.hasText(userForm.getUsername())
                || !StringUtils.hasText(userForm.getPassword())
                || !StringUtils.hasText(userForm.getRoleCode())) {
            throw new IllegalArgumentException("用户信息必填字段不能为空");
        }
    }
}