package com.qingyun.userportal.api.assistant;

import java.time.LocalDateTime;

public record AssistantConversationSummaryResponse(
        String conversationId,
        String title,
        String lastPreview,
        String status,
        LocalDateTime lastMessageAt,
        LocalDateTime updatedAt) {
}