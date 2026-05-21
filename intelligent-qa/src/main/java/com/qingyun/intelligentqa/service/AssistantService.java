package com.qingyun.intelligentqa.service;

import com.qingyun.intelligentqa.api.assistant.AssistantChatRequest;
import com.qingyun.intelligentqa.api.assistant.AssistantChatResponse;
import com.qingyun.intelligentqa.api.assistant.AssistantCitationResponse;
import com.qingyun.intelligentqa.api.assistant.AssistantConversationCreatedResponse;
import com.qingyun.intelligentqa.api.assistant.AssistantConversationMessagesResponse;
import com.qingyun.intelligentqa.api.assistant.AssistantConversationSummaryResponse;
import com.qingyun.intelligentqa.api.assistant.AssistantHealthResponse;
import com.qingyun.intelligentqa.api.assistant.AssistantMessageResponse;
import com.qingyun.intelligentqa.api.assistant.AssistantRuntimeConfigResponse;
import com.qingyun.intelligentqa.domain.AssistantCitation;
import com.qingyun.intelligentqa.domain.AssistantConversation;
import com.qingyun.intelligentqa.domain.AssistantMessage;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AssistantService {

    private static final String DEFAULT_USER = "anonymous";
    private static final String DEFAULT_TITLE = "新对话";
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String PLACEHOLDER_STATUS = "PENDING_IMPLEMENTATION";

    private final AssistantRuntimeConfigService runtimeConfigService;
    private final ConcurrentMap<String, AssistantConversation> conversations = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CopyOnWriteArrayList<AssistantMessage>> messagesByConversation = new ConcurrentHashMap<>();

    public AssistantService(AssistantRuntimeConfigService runtimeConfigService) {
        this.runtimeConfigService = runtimeConfigService;
    }

    public AssistantConversationCreatedResponse createConversation(String userId) {
        AssistantConversation conversation = createConversationRecord(normalizeUserId(userId));
        return new AssistantConversationCreatedResponse(
                conversation.conversationId(),
                conversation.title(),
                conversation.createdAt());
    }

    public List<AssistantConversationSummaryResponse> listConversations(String userId) {
        String normalizedUserId = normalizeUserId(userId);
        return conversations.values().stream()
                .filter(conversation -> conversation.userId().equals(normalizedUserId))
                .sorted(Comparator.comparing(AssistantConversation::updatedAt).reversed())
                .map(this::toSummaryResponse)
                .toList();
    }

    public AssistantConversationMessagesResponse conversationMessages(String userId, String conversationId) {
        AssistantConversation conversation = requireConversation(normalizeUserId(userId), conversationId);
        List<AssistantMessageResponse> messages = messageList(conversationId).stream()
                .map(this::toMessageResponse)
                .toList();
        return new AssistantConversationMessagesResponse(
                conversation.conversationId(),
                conversation.title(),
                conversation.status(),
                messages);
    }

    public void deleteConversation(String userId, String conversationId) {
        AssistantConversation conversation = requireConversation(normalizeUserId(userId), conversationId);
        conversations.remove(conversation.conversationId());
        messagesByConversation.remove(conversation.conversationId());
    }

    public AssistantChatResponse chat(String userId, AssistantChatRequest request) {
        String normalizedUserId = normalizeUserId(userId);
        AssistantConversation conversation = StringUtils.hasText(request.conversationId())
                ? requireConversation(normalizedUserId, request.conversationId())
                : createConversationRecord(normalizedUserId);

        AssistantRuntimeConfigResponse runtimeConfig = runtimeConfigService.getRuntimeConfig();
        String trimmedQuestion = request.message().trim();
        LocalDateTime now = LocalDateTime.now();

        AssistantMessage userMessage = new AssistantMessage(
                UUID.randomUUID().toString(),
                conversation.conversationId(),
                "user",
                trimmedQuestion,
                "SUCCESS",
                null,
                null,
                List.of(),
                List.of(),
                now);
        appendMessage(conversation.conversationId(), userMessage);

        List<AssistantCitation> citations = buildCitations(trimmedQuestion);
        List<String> suggestions = buildSuggestions(trimmedQuestion);
        int latencyMs = 180 + Math.min(720, trimmedQuestion.length() * 12);
        String answer = buildPlaceholderAnswer(trimmedQuestion, citations);

        AssistantMessage assistantMessage = new AssistantMessage(
                UUID.randomUUID().toString(),
                conversation.conversationId(),
                "assistant",
                answer,
                PLACEHOLDER_STATUS,
                runtimeConfig.model().modelId(),
                latencyMs,
                suggestions,
                citations,
                LocalDateTime.now());
        appendMessage(conversation.conversationId(), assistantMessage);

        AssistantConversation refreshedConversation = refreshConversation(conversation, trimmedQuestion,
                assistantMessage.createdAt());
        conversations.put(refreshedConversation.conversationId(), refreshedConversation);

        return new AssistantChatResponse(
                refreshedConversation.conversationId(),
                userMessage.messageId(),
                assistantMessage.messageId(),
                PLACEHOLDER_STATUS,
                assistantMessage.content(),
                citations.stream().map(this::toCitationResponse).toList(),
                suggestions,
                runtimeConfig,
                assistantMessage.createdAt());
    }

    public AssistantRuntimeConfigResponse runtimeConfig() {
        return runtimeConfigService.getRuntimeConfig();
    }

    public AssistantHealthResponse health() {
        return new AssistantHealthResponse("UP", LocalDateTime.now(), conversations.size());
    }

    private AssistantConversation createConversationRecord(String userId) {
        LocalDateTime now = LocalDateTime.now();
        AssistantConversation conversation = new AssistantConversation(
                UUID.randomUUID().toString(),
                userId,
                DEFAULT_TITLE,
                ACTIVE_STATUS,
                null,
                now,
                now);
        conversations.put(conversation.conversationId(), conversation);
        messagesByConversation.put(conversation.conversationId(), new CopyOnWriteArrayList<>());
        return conversation;
    }

    private AssistantConversation requireConversation(String userId, String conversationId) {
        AssistantConversation conversation = conversations.get(conversationId);
        if (conversation == null || !conversation.userId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "会话不存在");
        }
        return conversation;
    }

    private void appendMessage(String conversationId, AssistantMessage message) {
        messagesByConversation.computeIfAbsent(conversationId, key -> new CopyOnWriteArrayList<>()).add(message);
    }

    private List<AssistantMessage> messageList(String conversationId) {
        return messagesByConversation.getOrDefault(conversationId, new CopyOnWriteArrayList<>());
    }

    private AssistantConversation refreshConversation(
            AssistantConversation conversation,
            String latestQuestion,
            LocalDateTime updatedAt) {
        String title = conversation.title();
        if (!StringUtils.hasText(title) || DEFAULT_TITLE.equals(title)) {
            title = buildConversationTitle(latestQuestion);
        }
        return new AssistantConversation(
                conversation.conversationId(),
                conversation.userId(),
                title,
                ACTIVE_STATUS,
                updatedAt,
                conversation.createdAt(),
                updatedAt);
    }

    private AssistantConversationSummaryResponse toSummaryResponse(AssistantConversation conversation) {
        List<AssistantMessage> messages = messageList(conversation.conversationId());
        String preview = messages.isEmpty()
                ? "等待你的第一条问题"
                : abbreviate(messages.get(messages.size() - 1).content(), 38);
        return new AssistantConversationSummaryResponse(
                conversation.conversationId(),
                conversation.title(),
                preview,
                conversation.status(),
                conversation.lastMessageAt(),
                conversation.updatedAt());
    }

    private AssistantMessageResponse toMessageResponse(AssistantMessage message) {
        return new AssistantMessageResponse(
                message.messageId(),
                message.role(),
                message.content(),
                message.status(),
                message.modelId(),
                message.latencyMs(),
                message.followUpSuggestions(),
                message.citations().stream().map(this::toCitationResponse).toList(),
                message.createdAt());
    }

    private AssistantCitationResponse toCitationResponse(AssistantCitation citation) {
        return new AssistantCitationResponse(
                citation.documentId(),
                citation.chunkId(),
                citation.documentTitle(),
                citation.sectionTitle(),
                citation.snippet(),
                citation.sourceUrl(),
                citation.score());
    }

    private String normalizeUserId(String userId) {
        return StringUtils.hasText(userId) ? userId.trim() : DEFAULT_USER;
    }

    private String buildConversationTitle(String question) {
        return abbreviate(question.replaceAll("\\s+", " "), 18);
    }

    private String buildPlaceholderAnswer(String question, List<AssistantCitation> citations) {
        String sourceSummary = citations.stream()
                .map(AssistantCitation::documentTitle)
                .distinct()
                .reduce((left, right) -> left + "、" + right)
                .orElse("当前知识库占位内容");
        return "这是 intelligent-qa 第一版的占位回答。系统已经完成会话创建、消息保存和接口编排，当前收到的问题是“"
                + question
                + "”。后续会在这里接入真实检索、重排和模型生成链路。当前建议优先参考 “"
                + sourceSummary
                + "” 相关文档片段继续完善答案。";
    }

    private List<AssistantCitation> buildCitations(String question) {
        String normalizedQuestion = question.toLowerCase(Locale.ROOT);
        if (normalizedQuestion.contains("spring boot") || normalizedQuestion.contains("自动配置")) {
            return List.of(
                    new AssistantCitation(
                            1001L,
                            9001L,
                            "Spring Boot Reference Documentation",
                            "Auto-configuration",
                            "Spring Boot 会基于自动配置清单和条件注解决定是否注册 Bean。",
                            "https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html",
                            0.96),
                    new AssistantCitation(
                            1002L,
                            9002L,
                            "Spring Framework Reference",
                            "Bean Definition Profiles and Conditions",
                            "条件装配通常依赖类路径、配置属性和上下文中已有 Bean。",
                            "https://docs.spring.io/spring-framework/reference/core/beans/environment.html",
                            0.89));
        }
        if (normalizedQuestion.contains("mybatis") || normalizedQuestion.contains("resultmap")) {
            return List.of(
                    new AssistantCitation(
                            2001L,
                            9101L,
                            "MyBatis User Guide",
                            "Result Maps",
                            "ResultMap 用于把查询结果映射到复杂对象结构，适合处理一对多和嵌套对象。",
                            "https://mybatis.org/mybatis-3/sqlmap-xml.html#Result_Maps",
                            0.93));
        }
        if (normalizedQuestion.contains("redis") || normalizedQuestion.contains("哨兵")
                || normalizedQuestion.contains("集群")) {
            return List.of(
                    new AssistantCitation(
                            3001L,
                            9201L,
                            "Redis Documentation",
                            "Redis Sentinel",
                            "Sentinel 更偏向高可用治理，而 Redis Cluster 同时解决水平扩展与分片问题。",
                            "https://redis.io/docs/latest/operate/oss_and_stack/management/sentinel/",
                            0.91),
                    new AssistantCitation(
                            3002L,
                            9202L,
                            "Redis Documentation",
                            "Redis Cluster Specification",
                            "Cluster 通过 hash slot 管理分片，并要求客户端具备重定向处理能力。",
                            "https://redis.io/docs/latest/operate/oss_and_stack/reference/cluster-spec/",
                            0.87));
        }
        return List.of(
                new AssistantCitation(
                        4001L,
                        9301L,
                        "JavaKQA Placeholder Knowledge",
                        "Question Routing",
                        "第一版目前使用占位知识片段，重点验证会话流、引用结构和模块拆分。",
                        "https://example.com/javakqa/placeholder",
                        0.78));
    }

    private List<String> buildSuggestions(String question) {
        String normalizedQuestion = question.toLowerCase(Locale.ROOT);
        if (normalizedQuestion.contains("自动配置") || normalizedQuestion.contains("spring boot")) {
            return List.of(
                    "继续追问：AutoConfiguration.imports 是如何被加载的？",
                    "继续追问：@ConditionalOnClass 和 @ConditionalOnMissingBean 有什么区别？");
        }
        if (normalizedQuestion.contains("事务") || normalizedQuestion.contains("transaction")) {
            return List.of(
                    "继续追问：REQUIRES_NEW 和 NESTED 的差异是什么？",
                    "继续追问：事务为什么会在同类方法调用时失效？");
        }
        return List.of(
                "继续追问：这类问题在真实 RAG 链路里会如何检索上下文？",
                "继续追问：如果要展示引用来源，前台应优先呈现哪些字段？");
    }

    private String abbreviate(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return DEFAULT_TITLE;
        }
        String normalized = text.trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxLength - 1)) + "…";
    }
}