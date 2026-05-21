package com.qingyun.intelligentqa.domain;

import java.time.LocalDateTime;
import java.util.List;

public record AssistantMessage(
        String messageId,
        String conversationId,
        String role,
        String content,
        String status,
        String modelId,
        Integer latencyMs,
        List<String> followUpSuggestions,
        List<AssistantCitation> citations,
        LocalDateTime createdAt) {
}