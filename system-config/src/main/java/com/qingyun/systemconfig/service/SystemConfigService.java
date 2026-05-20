package com.qingyun.systemconfig.service;

import com.qingyun.framework.security.LoginUser;
import com.qingyun.systemconfig.model.SystemConfigItem;
import com.qingyun.systemconfig.repository.SystemConfigRepository;
import com.qingyun.systemconfig.web.ConfigForm;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;
    private final OperationLogService operationLogService;

    public SystemConfigService(SystemConfigRepository systemConfigRepository,
            OperationLogService operationLogService) {
        this.systemConfigRepository = systemConfigRepository;
        this.operationLogService = operationLogService;
    }

    public Map<String, List<SystemConfigItem>> listGroupedConfigs() {
        return systemConfigRepository.findAll().stream()
                .collect(Collectors.groupingBy(SystemConfigItem::getConfigGroup, LinkedHashMap::new,
                        Collectors.toList()));
    }

    public void createConfig(ConfigForm configForm, LoginUser operator) {
        validateForm(configForm);
        if (systemConfigRepository.existsByConfigKey(configForm.getConfigKey())) {
            throw new IllegalArgumentException("配置标识已存在，请更换 config_key");
        }

        SystemConfigItem item = toEntity(configForm);
        LocalDateTime now = LocalDateTime.now();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        systemConfigRepository.insert(item);
        operationLogService.record(operator.id(), "SYSTEM_CONFIG", "CREATE", "新增配置项：" + configForm.getConfigKey());
    }

    public void updateConfig(Long id, ConfigForm configForm, LoginUser operator) {
        validateForm(configForm);
        if (systemConfigRepository.existsByConfigKeyExcludingId(configForm.getConfigKey(), id)) {
            throw new IllegalArgumentException("配置标识已被其他配置占用");
        }

        SystemConfigItem item = toEntity(configForm);
        item.setUpdatedAt(LocalDateTime.now());
        systemConfigRepository.update(id, item);
        operationLogService.record(operator.id(), "SYSTEM_CONFIG", "UPDATE", "更新配置项：" + configForm.getConfigKey());
    }

    public void updateStatus(Long id, boolean enabled, LoginUser operator) {
        systemConfigRepository.updateEnabled(id, enabled, LocalDateTime.now());
        operationLogService.record(operator.id(), "SYSTEM_CONFIG", enabled ? "ENABLE" : "DISABLE", "切换配置项状态，ID=" + id);
    }

    public void ensureDefaultConfig(String configKey,
            String configName,
            String configGroup,
            String configValue,
            String valueType,
            String description,
            boolean enabled) {
        if (systemConfigRepository.existsByConfigKey(configKey)) {
            return;
        }

        SystemConfigItem item = new SystemConfigItem();
        item.setConfigKey(configKey);
        item.setConfigName(configName);
        item.setConfigGroup(configGroup);
        item.setConfigValue(configValue);
        item.setValueType(valueType);
        item.setDescription(description);
        item.setEnabled(enabled);
        LocalDateTime now = LocalDateTime.now();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        systemConfigRepository.insert(item);
    }

    private SystemConfigItem toEntity(ConfigForm configForm) {
        SystemConfigItem item = new SystemConfigItem();
        item.setConfigKey(configForm.getConfigKey().trim());
        item.setConfigName(configForm.getConfigName().trim());
        item.setConfigGroup(configForm.getConfigGroup().trim());
        item.setConfigValue(configForm.getConfigValue().trim());
        item.setValueType(configForm.getValueType().trim());
        item.setDescription(configForm.getDescription());
        item.setEnabled(Boolean.TRUE.equals(configForm.getEnabled()));
        return item;
    }

    private void validateForm(ConfigForm configForm) {
        if (!StringUtils.hasText(configForm.getConfigKey())
                || !StringUtils.hasText(configForm.getConfigName())
                || !StringUtils.hasText(configForm.getConfigGroup())
                || !StringUtils.hasText(configForm.getConfigValue())
                || !StringUtils.hasText(configForm.getValueType())) {
            throw new IllegalArgumentException("配置项必填字段不能为空");
        }
    }
}