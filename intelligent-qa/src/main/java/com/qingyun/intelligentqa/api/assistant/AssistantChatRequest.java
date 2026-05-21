package com.qingyun.intelligentqa.api.assistant;

import jakarta.validation.constraints.NotBlank;

public record AssistantChatRequest(String conversationId, @NotBlank String message) {
}