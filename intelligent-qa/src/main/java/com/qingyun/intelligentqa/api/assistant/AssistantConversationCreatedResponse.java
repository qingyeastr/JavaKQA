package com.qingyun.intelligentqa.api.assistant;

import java.time.LocalDateTime;

public record AssistantConversationCreatedResponse(
        String conversationId,
        String title,
        LocalDateTime createdAt) {
}