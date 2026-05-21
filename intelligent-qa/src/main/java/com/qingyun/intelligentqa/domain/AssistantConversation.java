package com.qingyun.intelligentqa.domain;

import java.time.LocalDateTime;

public record AssistantConversation(
        String conversationId,
        String userId,
        String title,
        String status,
        LocalDateTime lastMessageAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}