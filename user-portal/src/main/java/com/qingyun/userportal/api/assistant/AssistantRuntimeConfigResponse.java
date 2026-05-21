package com.qingyun.userportal.api.assistant;

public record AssistantRuntimeConfigResponse(
        ModelConfig model,
        RetrievalConfig retrieval,
        PromptConfig prompt,
        SystemSwitchConfig system) {

    public record ModelConfig(
            String modelId,
            String apiBaseUrl,
            int timeoutMs,
            String embeddingModelName,
            int embeddingDimension) {
    }

    public record RetrievalConfig(
            int keywordTopK,
            int semanticTopK,
            double hybridWeight,
            boolean rerankEnabled,
            int rerankTopN,
            double similarityThreshold,
            int minRecallCount) {
    }

    public record PromptConfig(
            String systemTemplate,
            boolean queryRewriteEnabled,
            int contextWindowSize,
            int answerMaxLength,
            boolean referenceEnabled) {
    }

    public record SystemSwitchConfig(
            boolean knowledgeUploadEnabled,
            boolean feedbackEnabled,
            boolean analyticsEnabled,
            boolean sensitiveFilterEnabled) {
    }
}