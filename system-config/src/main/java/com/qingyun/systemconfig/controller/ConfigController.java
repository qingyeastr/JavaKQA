package com.qingyun.systemconfig.controller;

import com.qingyun.framework.security.LoginUser;
import com.qingyun.framework.web.SessionConstants;
import com.qingyun.systemconfig.service.SystemConfigService;
import com.qingyun.systemconfig.web.ConfigForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/configs")
public class ConfigController {

    private final SystemConfigService systemConfigService;

    public ConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @GetMapping
    public String configs(Model model) {
        preparePage(model);
        return "configs";
    }

    @PostMapping
    public String createConfig(@Valid @ModelAttribute("configForm") ConfigForm configForm,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            preparePage(model);
            model.addAttribute("formError", "请完整填写配置项必填字段");
            return "configs";
        }

        try {
            systemConfigService.createConfig(configForm, currentUser(session));
            redirectAttributes.addFlashAttribute("message", "配置项已创建");
            return "redirect:/configs";
        } catch (IllegalArgumentException exception) {
            preparePage(model);
            model.addAttribute("formError", exception.getMessage());
            return "configs";
        }
    }

    @PostMapping("/{id}/update")
    public String updateConfig(@PathVariable Long id,
            @RequestParam String configKey,
            @RequestParam String configName,
            @RequestParam String configGroup,
            @RequestParam String configValue,
            @RequestParam String valueType,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "false") boolean enabled,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            ConfigForm configForm = new ConfigForm();
            configForm.setConfigKey(configKey);
            configForm.setConfigName(configName);
            configForm.setConfigGroup(configGroup);
            configForm.setConfigValue(configValue);
            configForm.setValueType(valueType);
            configForm.setDescription(description);
            configForm.setEnabled(enabled);
            systemConfigService.updateConfig(id, configForm, currentUser(session));
            redirectAttributes.addFlashAttribute("message", "配置项已更新");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/configs";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
            @RequestParam boolean enabled,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        systemConfigService.updateStatus(id, enabled, currentUser(session));
        redirectAttributes.addFlashAttribute("message", enabled ? "配置项已启用" : "配置项已停用");
        return "redirect:/configs";
    }

    private void preparePage(Model model) {
        if (!model.containsAttribute("configForm")) {
            model.addAttribute("configForm", new ConfigForm());
        }
        model.addAttribute("groupedConfigs", systemConfigService.listGroupedConfigs());
    }

    private LoginUser currentUser(HttpSession session) {
        return (LoginUser) session.getAttribute(SessionConstants.LOGIN_USER);
    }
}