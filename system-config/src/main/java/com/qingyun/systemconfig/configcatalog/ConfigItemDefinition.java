package com.qingyun.systemconfig.configcatalog;

public record ConfigItemDefinition(
        String key,
        String name,
        String groupKey,
        String valueType,
        String defaultValue,
        String description,
        boolean defaultEnabled,
        int sortOrder,
        boolean sensitive) {
}