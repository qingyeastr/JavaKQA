package com.qingyun.userportal.api.assistant;

public record AssistantCitationResponse(
        Long documentId,
        Long chunkId,
        String documentTitle,
        String sectionTitle,
        String snippet,
        String sourceUrl,
        double score) {
}