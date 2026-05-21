package com.qingyun.systemconfig.service;

import com.qingyun.framework.security.LoginUser;
import com.qingyun.systemconfig.configcatalog.ConfigItemDefinition;
import com.qingyun.systemconfig.configcatalog.SystemConfigCatalog;
import com.qingyun.systemconfig.model.SystemConfigItem;
import com.qingyun.systemconfig.repository.SystemConfigRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SystemConfigService {

    private static final int DEFAULT_LLM_TIMEOUT_MS = 30000;

    private final SystemConfigRepository systemConfigRepository;
    private final OperationLogService operationLogService;

    public SystemConfigService(SystemConfigRepository systemConfigRepository,
            OperationLogService operationLogService) {
        this.systemConfigRepository = systemConfigRepository;
        this.operationLogService = operationLogService;
    }

    public Map<String, List<SystemConfigItem>> listGroupedConfigs() {
        Map<String, SystemConfigItem> existingItems = systemConfigRepository.findAll().stream()
                .filter(item -> SystemConfigCatalog.findItem(item.getConfigKey()).isPresent())
                .collect(Collectors.toMap(SystemConfigItem::getConfigKey, Function.identity(), (left, right) -> left));

        Map<String, List<SystemConfigItem>> groupedConfigs = new LinkedHashMap<>();
        SystemConfigCatalog.itemsGrouped().forEach((groupKey, definitions) -> {
            List<SystemConfigItem> items = definitions.stream()
                    .map(definition -> existingItems.get(definition.key()))
                    .filter(item -> item != null)
                    .sorted(Comparator.comparingInt(item -> SystemConfigCatalog.findItem(item.getConfigKey())
                            .map(ConfigItemDefinition::sortOrder)
                            .orElse(Integer.MAX_VALUE)))
                    .toList();
            groupedConfigs.put(groupKey, items);
        });
        return groupedConfigs;
    }

    public void syncFixedConfigs() {
        Map<String, SystemConfigItem> existingItems = systemConfigRepository.findAll().stream()
                .collect(Collectors.toMap(SystemConfigItem::getConfigKey, Function.identity(), (left, right) -> left));

        for (ConfigItemDefinition definition : SystemConfigCatalog.items()) {
            SystemConfigItem existingItem = existingItems.get(definition.key());
            if (existingItem == null) {
                insertDefinition(definition, legacyValueFor(definition.key(), existingItems));
                continue;
            }

            boolean changed = false;
            if (!definition.name().equals(existingItem.getConfigName())) {
                existingItem.setConfigName(definition.name());
                changed = true;
            }
            if (!definition.groupKey().equals(existingItem.getConfigGroup())) {
                existingItem.setConfigGroup(definition.groupKey());
                changed = true;
            }
            if (!definition.valueType().equals(existingItem.getValueType())) {
                existingItem.setValueType(definition.valueType());
                changed = true;
            }
            if (!definition.description().equals(existingItem.getDescription())) {
                existingItem.setDescription(definition.description());
                changed = true;
            }
            String normalizedCatalogValue = normalizeCatalogValue(definition, existingItem.getConfigValue());
            if (StringUtils.hasText(normalizedCatalogValue)
                    && !normalizedCatalogValue.equals(existingItem.getConfigValue())) {
                existingItem.setConfigValue(normalizedCatalogValue);
                changed = true;
            }
            if (!StringUtils.hasText(existingItem.getConfigValue())) {
                existingItem.setConfigValue(definition.defaultValue());
                changed = true;
            }
            if (changed) {
                existingItem.setUpdatedAt(LocalDateTime.now());
                systemConfigRepository.update(existingItem.getId(), existingItem);
            }
        }
    }

    public void updateFixedConfig(Long id, String configKey, String configValue, boolean enabled, LoginUser operator) {
        ConfigItemDefinition definition = SystemConfigCatalog.findItem(configKey)
                .orElseThrow(() -> new IllegalArgumentException("仅支持维护开发文档中定义的固定配置项"));

        SystemConfigItem existingItem = systemConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("配置项不存在或已被删除"));

        if (!configKey.equals(existingItem.getConfigKey())) {
            throw new IllegalArgumentException("配置项标识不匹配，无法更新");
        }

        existingItem.setConfigName(definition.name());
        existingItem.setConfigGroup(definition.groupKey());
        existingItem.setValueType(definition.valueType());
        existingItem.setDescription(definition.description());
        existingItem.setConfigValue(normalizeConfigValue(definition, configValue, existingItem.getConfigValue()));
        existingItem.setEnabled(enabled);
        existingItem.setUpdatedAt(LocalDateTime.now());
        systemConfigRepository.update(id, existingItem);
        operationLogService.record(operator.id(), "SYSTEM_CONFIG", "UPDATE", "更新固定配置项：" + existingItem.getConfigKey());
    }

    public void updateModelLlmSettings(String modelId, String apiBaseUrl, String apiKey, LoginUser operator) {
        Map<String, SystemConfigItem> existingItems = systemConfigRepository.findAll().stream()
                .collect(Collectors.toMap(SystemConfigItem::getConfigKey, Function.identity(), (left, right) -> left));

        updateModelLlmItem(existingItems, "model.llm.model-id", modelId);
        updateModelLlmItem(existingItems, "model.llm.api-base-url", apiBaseUrl);
        updateModelLlmItem(existingItems, "model.llm.api-key", apiKey);

        operationLogService.record(operator.id(), "SYSTEM_CONFIG", "UPDATE", "更新大语言模型配置");
    }

    private void insertDefinition(ConfigItemDefinition definition, String configValueOverride) {
        SystemConfigItem item = new SystemConfigItem();
        item.setConfigKey(definition.key());
        item.setConfigName(definition.name());
        item.setConfigGroup(definition.groupKey());
        String initialValue = StringUtils.hasText(configValueOverride) ? configValueOverride
                : definition.defaultValue();
        item.setConfigValue(normalizeCatalogValue(definition, initialValue));
        item.setValueType(definition.valueType());
        item.setDescription(definition.description());
        item.setEnabled(definition.defaultEnabled());
        LocalDateTime now = LocalDateTime.now();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        systemConfigRepository.insert(item);
    }

    private String normalizeConfigValue(ConfigItemDefinition definition, String rawValue, String currentValue) {
        String trimmed = rawValue == null ? "" : rawValue.trim();
        if (definition.sensitive() && !StringUtils.hasText(trimmed)) {
            return currentValue;
        }
        if (!StringUtils.hasText(trimmed)) {
            throw new IllegalArgumentException("配置值不能为空");
        }

        return switch (definition.valueType()) {
            case "number" -> normalizeNumber(trimmed);
            case "boolean" -> normalizeBoolean(trimmed);
            case "json" -> normalizeJson(trimmed);
            default -> trimmed;
        };
    }

    private String normalizeNumber(String value) {
        try {
            new java.math.BigDecimal(value);
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("数值型配置必须填写合法数字");
        }
    }

    private String normalizeBoolean(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        if (!"true".equals(normalized) && !"false".equals(normalized)) {
            throw new IllegalArgumentException("布尔型配置只能填写 true 或 false");
        }
        return normalized;
    }

    private String normalizeJson(String value) {
        String trimmed = value.trim();
        boolean objectLike = trimmed.startsWith("{") && trimmed.endsWith("}");
        boolean arrayLike = trimmed.startsWith("[") && trimmed.endsWith("]");
        if (!objectLike && !arrayLike) {
            throw new IllegalArgumentException("JSON 类型配置需要以 {} 或 [] 结构保存");
        }
        return trimmed;
    }

    private String stringValue(Map<String, SystemConfigItem> configIndex, String key) {
        SystemConfigItem item = configIndex.get(key);
        if (item != null && StringUtils.hasText(item.getConfigValue())) {
            return item.getConfigValue();
        }
        return SystemConfigCatalog.findItem(key).map(ConfigItemDefinition::defaultValue).orElse("");
    }

    private String stringValue(Map<String, SystemConfigItem> configIndex, String primaryKey, String fallbackKey) {
        String primaryValue = stringValue(configIndex, primaryKey);
        if (StringUtils.hasText(primaryValue)) {
            return primaryValue;
        }
        return stringValue(configIndex, fallbackKey);
    }

    private int intValue(Map<String, SystemConfigItem> configIndex, String key) {
        return Integer.parseInt(stringValue(configIndex, key));
    }

    private int intValue(Map<String, SystemConfigItem> configIndex, String key, int defaultValue) {
        String value = stringValue(configIndex, key);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    private double doubleValue(Map<String, SystemConfigItem> configIndex, String key) {
        return Double.parseDouble(stringValue(configIndex, key));
    }

    private boolean booleanValue(Map<String, SystemConfigItem> configIndex, String key) {
        return Boolean.parseBoolean(stringValue(configIndex, key));
    }

    private String legacyValueFor(String configKey, Map<String, SystemConfigItem> existingItems) {
        return switch (configKey) {
            case "model.llm.model-id" -> readConfigValue(existingItems, "model.llm.default-name");
            case "model.llm.api-base-url" -> readConfigValue(existingItems, "model.llm.api-url");
            default -> null;
        };
    }

    private String readConfigValue(Map<String, SystemConfigItem> existingItems, String key) {
        SystemConfigItem item = existingItems.get(key);
        if (item == null || !StringUtils.hasText(item.getConfigValue())) {
            return null;
        }
        return item.getConfigValue();
    }

    private String normalizeCatalogValue(ConfigItemDefinition definition, String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        if (!"model.llm.api-base-url".equals(definition.key())) {
            return value;
        }

        String trimmed = value.trim();
        if (trimmed.endsWith("/chat/completions")) {
            return trimmed.substring(0, trimmed.length() - "/chat/completions".length());
        }
        if (trimmed.endsWith("/messages")) {
            return trimmed.substring(0, trimmed.length() - "/messages".length());
        }
        if (trimmed.endsWith("/completions")) {
            return trimmed.substring(0, trimmed.length() - "/completions".length());
        }
        return trimmed;
    }

    private void updateModelLlmItem(Map<String, SystemConfigItem> existingItems, String configKey, String rawValue) {
        ConfigItemDefinition definition = SystemConfigCatalog.findItem(configKey)
                .orElseThrow(() -> new IllegalArgumentException("模型配置项定义不存在：" + configKey));
        SystemConfigItem existingItem = existingItems.get(configKey);
        if (existingItem == null) {
            throw new IllegalArgumentException("模型配置项不存在，请刷新页面后重试");
        }

        existingItem.setConfigName(definition.name());
        existingItem.setConfigGroup(definition.groupKey());
        existingItem.setValueType(definition.valueType());
        existingItem.setDescription(definition.description());
        existingItem.setConfigValue(normalizeCatalogValue(definition,
                normalizeConfigValue(definition, rawValue, existingItem.getConfigValue())));
        existingItem.setUpdatedAt(LocalDateTime.now());
        systemConfigRepository.update(existingItem.getId(), existingItem);
    }
}