package com.qingyun.systemconfig.controller;

import com.qingyun.systemconfig.api.assistant.AssistantChatRequest;
import com.qingyun.systemconfig.api.assistant.AssistantChatResponse;
import com.qingyun.systemconfig.api.assistant.AssistantRuntimeConfigResponse;
import com.qingyun.systemconfig.service.SystemConfigService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
public class AssistantApiController {

    private final SystemConfigService systemConfigService;

    public AssistantApiController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @GetMapping("/runtime-config")
    public AssistantRuntimeConfigResponse runtimeConfig() {
        return systemConfigService.getAssistantRuntimeConfig();
    }

    @PostMapping("/chat")
    public AssistantChatResponse chat(@Valid @RequestBody AssistantChatRequest request) {
        AssistantRuntimeConfigResponse runtimeConfig = systemConfigService.getAssistantRuntimeConfig();
        String sessionId = StringUtils.hasText(request.sessionId()) ? request.sessionId()
                : UUID.randomUUID().toString();
        return new AssistantChatResponse(
                sessionId,
                "PENDING_IMPLEMENTATION",
                "前台智能助手对话接口已预留，当前尚未接入知识库检索、RAG 编排和大模型生成链路。",
                List.of(
                        "下一步可在该接口接入文档检索与重排。",
                        "随后可拼装 Prompt 并调用大语言模型生成答案。"),
                runtimeConfig);
    }
}