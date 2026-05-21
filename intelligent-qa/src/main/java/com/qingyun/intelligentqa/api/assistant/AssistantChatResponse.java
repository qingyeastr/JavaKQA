package com.qingyun.intelligentqa.api.assistant;

import java.time.LocalDateTime;
import java.util.List;

public record AssistantChatResponse(
        String conversationId,
        String userMessageId,
        String assistantMessageId,
        String status,
        String answer,
        List<AssistantCitationResponse> citations,
        List<String> followUpSuggestions,
        AssistantRuntimeConfigResponse runtimeConfig,
        LocalDateTime createdAt) {
}