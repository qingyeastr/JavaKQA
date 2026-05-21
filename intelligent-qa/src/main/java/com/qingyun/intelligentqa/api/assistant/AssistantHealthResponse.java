package com.qingyun.intelligentqa.api.assistant;

import java.time.LocalDateTime;

public record AssistantHealthResponse(String status, LocalDateTime checkedAt, int conversationCount) {
}