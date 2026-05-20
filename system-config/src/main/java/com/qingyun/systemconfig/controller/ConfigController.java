package com.qingyun.systemconfig.controller;

import com.qingyun.framework.security.LoginUser;
import com.qingyun.framework.web.SessionConstants;
import com.qingyun.systemconfig.configcatalog.ConfigGroupDefinition;
import com.qingyun.systemconfig.configcatalog.ConfigItemDefinition;
import com.qingyun.systemconfig.configcatalog.SystemConfigCatalog;
import com.qingyun.systemconfig.model.SystemConfigItem;
import com.qingyun.systemconfig.service.SystemConfigService;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/configs")
public class ConfigController {

    private final SystemConfigService systemConfigService;

    public ConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @GetMapping
    public String configs(@RequestParam(required = false) String group, Model model) {
        preparePage(model, group);
        return "configs";
    }

    @PostMapping("/{id}/update")
    public String updateConfig(@PathVariable Long id,
            @RequestParam String configKey,
            @RequestParam String configValue,
            @RequestParam(defaultValue = "false") boolean enabled,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String group = resolveGroupByConfigKey(configKey);
        try {
            systemConfigService.updateFixedConfig(id, configKey, configValue, enabled, currentUser(session));
            redirectAttributes.addFlashAttribute("message", "配置项已更新");
            return redirectToGroup(group);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return redirectToGroup(group);
        }
    }

    @PostMapping("/model-llm/update")
    public String updateModelLlmSettings(@RequestParam String modelId,
            @RequestParam String apiBaseUrl,
            @RequestParam(required = false) String apiKey,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            systemConfigService.updateModelLlmSettings(modelId, apiBaseUrl, apiKey, currentUser(session));
            redirectAttributes.addFlashAttribute("message", "模型配置已更新");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return redirectToGroup("model");
    }

    private void preparePage(Model model, String preferredGroup) {
        Map<String, List<SystemConfigItem>> groupedConfigs = systemConfigService.listGroupedConfigs();
        String activeGroup = resolveActiveGroup(preferredGroup);
        List<SystemConfigItem> activeConfigs = StringUtils.hasText(activeGroup)
                ? groupedConfigs.getOrDefault(activeGroup, List.of())
                : List.of();

        model.addAttribute("groupedConfigs", groupedConfigs);
        model.addAttribute("configGroups", SystemConfigCatalog.groups());
        model.addAttribute("configDefinitions", SystemConfigCatalog.items().stream()
                .collect(Collectors.toMap(ConfigItemDefinition::key, Function.identity(), (left, right) -> left,
                        LinkedHashMap::new)));
        model.addAttribute("activeGroup", activeGroup);
        model.addAttribute("activeGroupDefinition", StringUtils.hasText(activeGroup)
                ? SystemConfigCatalog.findGroup(activeGroup).orElse(null)
                : null);
        model.addAttribute("activeConfigs", activeConfigs);
        Map<String, SystemConfigItem> activeConfigMap = activeConfigs.stream()
                .collect(Collectors.toMap(SystemConfigItem::getConfigKey, Function.identity(), (left, right) -> left,
                        LinkedHashMap::new));
        model.addAttribute("modelLlmModelIdConfig", activeConfigMap.get("model.llm.model-id"));
        model.addAttribute("modelLlmApiBaseUrlConfig", activeConfigMap.get("model.llm.api-base-url"));
        model.addAttribute("modelLlmApiKeyConfig", activeConfigMap.get("model.llm.api-key"));
        model.addAttribute("modelEmbeddingConfigs", activeConfigs.stream()
                .filter(item -> item.getConfigKey().startsWith("model.embedding."))
                .toList());
    }

    private LoginUser currentUser(HttpSession session) {
        return (LoginUser) session.getAttribute(SessionConstants.LOGIN_USER);
    }

    private String resolveActiveGroup(String preferredGroup) {
        if (StringUtils.hasText(preferredGroup)
                && SystemConfigCatalog.findGroup(preferredGroup.trim()).isPresent()) {
            return preferredGroup.trim();
        }
        return SystemConfigCatalog.groups().stream().findFirst().map(ConfigGroupDefinition::key).orElse(null);
    }

    private String redirectToGroup(String group) {
        if (!StringUtils.hasText(group)) {
            return "redirect:/configs";
        }
        return "redirect:/configs?group=" + UriUtils.encodeQueryParam(group.trim(), StandardCharsets.UTF_8);
    }

    private String resolveGroupByConfigKey(String configKey) {
        return SystemConfigCatalog.findItem(configKey).map(ConfigItemDefinition::groupKey).orElse(null);
    }
}