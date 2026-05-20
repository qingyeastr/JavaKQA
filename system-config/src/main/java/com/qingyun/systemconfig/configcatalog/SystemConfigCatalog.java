package com.qingyun.systemconfig.configcatalog;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class SystemConfigCatalog {

    private static final List<ConfigGroupDefinition> GROUPS = List.of(
            new ConfigGroupDefinition("model", "模型配置", "管理当前大语言模型配置与 Embedding 参数。", 10),
            new ConfigGroupDefinition("retrieval", "检索参数配置", "管理关键词检索、语义检索、混合检索和重排参数。", 20),
            new ConfigGroupDefinition("prompt", "问答参数配置", "管理 Prompt、查询改写、多轮上下文和答案展示策略。", 30),
            new ConfigGroupDefinition("system", "系统开关配置", "管理知识库上传、用户反馈、数据分析与内容审核开关。", 40));

    private static final List<ConfigItemDefinition> ITEMS = List.of(
            new ConfigItemDefinition("model.llm.model-id", "当前模型 ID", "model", "string", "gpt-4.1", "当前用于智能问答生成的大语言模型 ID，例如 gpt-4.1、deepseek-chat。", true, 10, false),
            new ConfigItemDefinition("model.llm.api-base-url", "请求地址（API Base URL）", "model", "string", "https://api.openai.com/v1", "当前模型供应商的请求基址，用于拼接实际接口调用。", true, 20, false),
            new ConfigItemDefinition("model.llm.api-key", "模型 API Key", "model", "string", "sk-replace-with-real-key", "访问当前模型供应商所需的 API Key。", true, 30, true),
            new ConfigItemDefinition("model.embedding.name", "Embedding 模型名称", "model", "string", "text-embedding-3-large", "知识片段和用户问题向量化所使用的 Embedding 模型名称。", true, 40, false),
            new ConfigItemDefinition("model.embedding.dimension", "向量维度", "model", "number", "3072", "Embedding 输出向量维度。", true, 50, false),
            new ConfigItemDefinition("model.embedding.api-url", "Embedding 接口地址", "model", "string", "https://api.openai.com/v1/embeddings", "Embedding 模型调用接口地址。", true, 60, false),

            new ConfigItemDefinition("retrieval.keyword.top-k", "关键词检索 TopK", "retrieval", "number", "10", "关键词检索阶段返回的候选片段数量。", true, 10, false),
            new ConfigItemDefinition("retrieval.semantic.top-k", "语义检索 TopK", "retrieval", "number", "8", "语义检索阶段返回的候选片段数量。", true, 20, false),
            new ConfigItemDefinition("retrieval.hybrid.weight", "混合检索权重", "retrieval", "number", "0.60", "关键词检索与语义检索结果融合时的权重。", true, 30, false),
            new ConfigItemDefinition("retrieval.rerank.enabled", "启用重排", "retrieval", "boolean", "true", "是否在召回结果后启用重排。", true, 40, false),
            new ConfigItemDefinition("retrieval.rerank.top-n", "重排数量", "retrieval", "number", "5", "进入重排阶段的候选片段数量。", true, 50, false),
            new ConfigItemDefinition("retrieval.similarity-threshold", "相似度阈值", "retrieval", "number", "0.75", "语义检索最小相似度阈值。", true, 60, false),
            new ConfigItemDefinition("retrieval.min-recall-count", "最低召回条数", "retrieval", "number", "3", "问答前至少需要保留的召回片段数量。", true, 70, false),

            new ConfigItemDefinition("prompt.system.template", "系统提示词模板", "prompt", "string", "你是一个面向 Java 后端技术文档的智能问答助手，请优先基于检索到的知识片段回答，并在有引用时明确给出来源。", "问答链路默认使用的系统提示词模板。", true, 10, false),
            new ConfigItemDefinition("prompt.query-rewrite.enabled", "启用查询改写", "prompt", "boolean", "true", "是否在检索前对用户问题进行查询改写。", true, 20, false),
            new ConfigItemDefinition("prompt.context.window-size", "多轮上下文窗口长度", "prompt", "number", "6", "多轮对话时保留的历史消息轮次。", true, 30, false),
            new ConfigItemDefinition("prompt.answer.max-length", "答案最大长度", "prompt", "number", "1200", "生成答案允许的最大字符长度。", true, 40, false),
            new ConfigItemDefinition("prompt.reference.enabled", "展示引用来源", "prompt", "boolean", "true", "是否在问答结果中展示知识片段引用来源。", true, 50, false),

            new ConfigItemDefinition("system.knowledge-upload.enabled", "启用知识库上传功能", "system", "boolean", "true", "控制后台是否开放知识库上传入口。", true, 10, false),
            new ConfigItemDefinition("system.feedback.enabled", "启用用户反馈功能", "system", "boolean", "true", "控制前台是否展示点赞点踩与纠错反馈入口。", true, 20, false),
            new ConfigItemDefinition("system.analytics.enabled", "启用数据分析面板", "system", "boolean", "true", "控制后台是否展示数据分析与统计面板。", true, 30, false),
            new ConfigItemDefinition("system.sensitive-filter.enabled", "启用敏感词过滤", "system", "boolean", "false", "控制问答内容是否启用敏感词过滤或内容审核。", true, 40, false));

    private static final Map<String, ConfigGroupDefinition> GROUP_INDEX = GROUPS.stream()
            .collect(Collectors.toMap(ConfigGroupDefinition::key, Function.identity(), (left, right) -> left, LinkedHashMap::new));

    private static final Map<String, ConfigItemDefinition> ITEM_INDEX = ITEMS.stream()
            .collect(Collectors.toMap(ConfigItemDefinition::key, Function.identity(), (left, right) -> left, LinkedHashMap::new));

    private SystemConfigCatalog() {
    }

    public static List<ConfigGroupDefinition> groups() {
        return GROUPS;
    }

    public static List<ConfigItemDefinition> items() {
        return ITEMS;
    }

    public static Optional<ConfigItemDefinition> findItem(String key) {
        return Optional.ofNullable(ITEM_INDEX.get(key));
    }

    public static Optional<ConfigGroupDefinition> findGroup(String key) {
        return Optional.ofNullable(GROUP_INDEX.get(key));
    }

    public static Map<String, List<ConfigItemDefinition>> itemsGrouped() {
        return ITEMS.stream().collect(Collectors.groupingBy(ConfigItemDefinition::groupKey,
                LinkedHashMap::new,
                Collectors.collectingAndThen(Collectors.toList(), list -> list.stream()
                        .sorted(Comparator.comparingInt(ConfigItemDefinition::sortOrder))
                        .toList())));
    }
}