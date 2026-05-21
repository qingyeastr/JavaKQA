package com.qingyun.intelligentqa.domain;

public record AssistantCitation(
        Long documentId,
        Long chunkId,
        String documentTitle,
        String sectionTitle,
        String snippet,
        String sourceUrl,
        double score) {
}