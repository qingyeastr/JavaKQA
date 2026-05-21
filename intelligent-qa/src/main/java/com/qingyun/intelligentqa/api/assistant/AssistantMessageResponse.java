package com.qingyun.intelligentqa.api.assistant;

import java.time.LocalDateTime;
import java.util.List;

public record AssistantMessageResponse(
        String messageId,
        String role,
        String content,
        String status,
        String modelId,
        Integer latencyMs,
        List<String> followUpSuggestions,
        List<AssistantCitationResponse> citations,
        LocalDateTime createdAt) {
}