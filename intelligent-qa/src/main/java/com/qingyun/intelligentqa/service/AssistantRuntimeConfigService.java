package com.qingyun.intelligentqa.service;

import com.qingyun.intelligentqa.api.assistant.AssistantRuntimeConfigResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AssistantRuntimeConfigService {

    private final String modelId;
    private final String apiBaseUrl;
    private final int timeoutMs;
    private final String embeddingModelName;
    private final int embeddingDimension;
    private final int keywordTopK;
    private final int semanticTopK;
    private final double hybridWeight;
    private final boolean rerankEnabled;
    private final int rerankTopN;
    private final double similarityThreshold;
    private final int minRecallCount;
    private final String systemTemplate;
    private final boolean queryRewriteEnabled;
    private final int contextWindowSize;
    private final int answerMaxLength;
    private final boolean referenceEnabled;
    private final boolean knowledgeUploadEnabled;
    private final boolean feedbackEnabled;
    private final boolean analyticsEnabled;
    private final boolean sensitiveFilterEnabled;

    public AssistantRuntimeConfigService(
            @Value("${app.assistant.runtime.model-id}") String modelId,
            @Value("${app.assistant.runtime.api-base-url}") String apiBaseUrl,
            @Value("${app.assistant.runtime.timeout-ms}") int timeoutMs,
            @Value("${app.assistant.runtime.embedding-model-name}") String embeddingModelName,
            @Value("${app.assistant.runtime.embedding-dimension}") int embeddingDimension,
            @Value("${app.assistant.runtime.keyword-top-k}") int keywordTopK,
            @Value("${app.assistant.runtime.semantic-top-k}") int semanticTopK,
            @Value("${app.assistant.runtime.hybrid-weight}") double hybridWeight,
            @Value("${app.assistant.runtime.rerank-enabled}") boolean rerankEnabled,
            @Value("${app.assistant.runtime.rerank-top-n}") int rerankTopN,
            @Value("${app.assistant.runtime.similarity-threshold}") double similarityThreshold,
            @Value("${app.assistant.runtime.min-recall-count}") int minRecallCount,
            @Value("${app.assistant.runtime.system-template}") String systemTemplate,
            @Value("${app.assistant.runtime.query-rewrite-enabled}") boolean queryRewriteEnabled,
            @Value("${app.assistant.runtime.context-window-size}") int contextWindowSize,
            @Value("${app.assistant.runtime.answer-max-length}") int answerMaxLength,
            @Value("${app.assistant.runtime.reference-enabled}") boolean referenceEnabled,
            @Value("${app.assistant.runtime.knowledge-upload-enabled}") boolean knowledgeUploadEnabled,
            @Value("${app.assistant.runtime.feedback-enabled}") boolean feedbackEnabled,
            @Value("${app.assistant.runtime.analytics-enabled}") boolean analyticsEnabled,
            @Value("${app.assistant.runtime.sensitive-filter-enabled}") boolean sensitiveFilterEnabled) {
        this.modelId = modelId;
        this.apiBaseUrl = apiBaseUrl;
        this.timeoutMs = timeoutMs;
        this.embeddingModelName = embeddingModelName;
        this.embeddingDimension = embeddingDimension;
        this.keywordTopK = keywordTopK;
        this.semanticTopK = semanticTopK;
        this.hybridWeight = hybridWeight;
        this.rerankEnabled = rerankEnabled;
        this.rerankTopN = rerankTopN;
        this.similarityThreshold = similarityThreshold;
        this.minRecallCount = minRecallCount;
        this.systemTemplate = systemTemplate;
        this.queryRewriteEnabled = queryRewriteEnabled;
        this.contextWindowSize = contextWindowSize;
        this.answerMaxLength = answerMaxLength;
        this.referenceEnabled = referenceEnabled;
        this.knowledgeUploadEnabled = knowledgeUploadEnabled;
        this.feedbackEnabled = feedbackEnabled;
        this.analyticsEnabled = analyticsEnabled;
        this.sensitiveFilterEnabled = sensitiveFilterEnabled;
    }

    public AssistantRuntimeConfigResponse getRuntimeConfig() {
        return new AssistantRuntimeConfigResponse(
                new AssistantRuntimeConfigResponse.ModelConfig(
                        modelId,
                        apiBaseUrl,
                        timeoutMs,
                        embeddingModelName,
                        embeddingDimension),
                new AssistantRuntimeConfigResponse.RetrievalConfig(
                        keywordTopK,
                        semanticTopK,
                        hybridWeight,
                        rerankEnabled,
                        rerankTopN,
                        similarityThreshold,
                        minRecallCount),
                new AssistantRuntimeConfigResponse.PromptConfig(
                        systemTemplate,
                        queryRewriteEnabled,
                        contextWindowSize,
                        answerMaxLength,
                        referenceEnabled),
                new AssistantRuntimeConfigResponse.SystemSwitchConfig(
                        knowledgeUploadEnabled,
                        feedbackEnabled,
                        analyticsEnabled,
                        sensitiveFilterEnabled));
    }
}