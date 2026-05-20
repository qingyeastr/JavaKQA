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
        systemConfigService.ensureDefaultConfig(
                "model.llm.default-name",
                "默认大模型",
                "model",
                "gpt-4.1",
                "string",
                "当前默认使用的大语言模型名称",
                true);
        systemConfigService.ensureDefaultConfig(
                "retrieval.keyword.top-k",
                "关键词检索 TopK",
                "retrieval",
                "10",
                "number",
                "关键词检索返回条数",
                true);
        systemConfigService.ensureDefaultConfig(
                "retrieval.semantic.top-k",
                "语义检索 TopK",
                "retrieval",
                "8",
                "number",
                "语义检索返回条数",
                true);
        systemConfigService.ensureDefaultConfig(
                "prompt.system.template",
                "系统提示词模板",
                "prompt",
                "你是一个面向 Java 后端技术文档的问答助手，请结合检索结果回答问题。",
                "string",
                "问答链路默认使用的系统提示词",
                true);
        systemConfigService.ensureDefaultConfig(
                "system.feedback.enabled",
                "启用用户反馈",
                "system",
                "true",
                "boolean",
                "控制是否开放用户反馈入口",
                true);
    }
}