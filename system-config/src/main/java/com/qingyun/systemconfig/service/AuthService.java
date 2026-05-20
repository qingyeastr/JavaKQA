package com.qingyun.systemconfig.service;

import com.qingyun.framework.security.LoginUser;
import com.qingyun.systemconfig.model.SysUser;
import com.qingyun.systemconfig.repository.SysUserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final OperationLogService operationLogService;

    public AuthService(SysUserRepository sysUserRepository,
            PasswordEncoder passwordEncoder,
            OperationLogService operationLogService) {
        this.sysUserRepository = sysUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.operationLogService = operationLogService;
    }

    public Optional<LoginUser> authenticate(String username, String rawPassword) {
        Optional<SysUser> userOptional = sysUserRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        SysUser sysUser = userOptional.get();
        if (!sysUser.isActive() || !passwordEncoder.matches(rawPassword, sysUser.getPassword())) {
            return Optional.empty();
        }

        LocalDateTime now = LocalDateTime.now();
        sysUserRepository.updateLastLoginTime(sysUser.getId(), now);
        operationLogService.record(sysUser.getId(), "AUTH", "LOGIN", "管理员登录系统");
        return Optional.of(new LoginUser(sysUser.getId(), sysUser.getUsername(), sysUser.getRoleCode()));
    }

    public void recordLogout(LoginUser loginUser) {
        operationLogService.record(loginUser.id(), "AUTH", "LOGOUT", "管理员退出系统");
    }
}