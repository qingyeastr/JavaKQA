package com.qingyun.systemconfig.api.assistant;

import jakarta.validation.constraints.NotBlank;

public record AssistantChatRequest(String sessionId, @NotBlank String message) {
}