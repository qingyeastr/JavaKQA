package com.qingyun.intelligentqa.api.assistant;

import java.util.List;

public record AssistantConversationMessagesResponse(
        String conversationId,
        String title,
        String status,
        List<AssistantMessageResponse> messages) {
}