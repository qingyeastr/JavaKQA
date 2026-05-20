package com.qingyun.systemconfig.api.assistant;

import java.util.List;

public record AssistantChatResponse(
        String sessionId,
        String status,
        String answer,
        List<String> nextActions,
        AssistantRuntimeConfigResponse runtimeConfig) {
}