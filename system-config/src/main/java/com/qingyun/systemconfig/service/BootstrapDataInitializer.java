package com.qingyun.systemconfig.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class BootstrapDataInitializer implements ApplicationRunner {

    private final UserService userService;
    private final SystemConfigService systemConfigService;

    public BootstrapDataInitializer(UserService userService, SystemConfigService systemConfigService) {
        this.userService = userService;
        this.systemConfigService = systemConfigService;
    }

    @Override
    public void run(ApplicationArguments args) {
        userService.ensureAdminUser();
        systemConfigService.syncFixedConfigs();
    }
}