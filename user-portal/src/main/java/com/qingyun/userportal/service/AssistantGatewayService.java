package com.qingyun.userportal.service;

import com.qingyun.userportal.api.assistant.AssistantChatRequest;
import com.qingyun.userportal.api.assistant.AssistantChatResponse;
import com.qingyun.userportal.api.assistant.AssistantConversationCreatedResponse;
import com.qingyun.userportal.api.assistant.AssistantConversationMessagesResponse;
import com.qingyun.userportal.api.assistant.AssistantConversationSummaryResponse;
import com.qingyun.userportal.api.assistant.AssistantRuntimeConfigResponse;
import com.qingyun.userportal.web.PortalUserSession;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AssistantGatewayService {

    private static final String USER_HEADER = "X-Portal-User";

    private final RestClient restClient;

    public AssistantGatewayService(@Value("${app.assistant-base-url}") String assistantBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(normalizeBaseUrl(assistantBaseUrl))
                .build();
    }

    public AssistantRuntimeConfigResponse runtimeConfig(PortalUserSession portalUser) {
        return restClient.get()
                .uri("/api/assistant/runtime-config")
                .header(USER_HEADER, portalUser.username())
                .retrieve()
                .body(AssistantRuntimeConfigResponse.class);
    }

    public List<AssistantConversationSummaryResponse> listConversations(PortalUserSession portalUser) {
        AssistantConversationSummaryResponse[] response = restClient.get()
                .uri("/api/assistant/conversations")
                .header(USER_HEADER, portalUser.username())
                .retrieve()
                .body(AssistantConversationSummaryResponse[].class);
        return response == null ? List.of() : List.of(response);
    }

    public AssistantConversationCreatedResponse createConversation(PortalUserSession portalUser) {
        return restClient.post()
                .uri("/api/assistant/conversations")
                .header(USER_HEADER, portalUser.username())
                .retrieve()
                .body(AssistantConversationCreatedResponse.class);
    }

    public AssistantConversationMessagesResponse conversationMessages(PortalUserSession portalUser, String conversationId) {
        return restClient.get()
                .uri("/api/assistant/conversations/{conversationId}/messages", conversationId)
                .header(USER_HEADER, portalUser.username())
                .retrieve()
                .body(AssistantConversationMessagesResponse.class);
    }

    public AssistantChatResponse chat(PortalUserSession portalUser, AssistantChatRequest request) {
        return restClient.post()
                .uri("/api/assistant/chat")
                .header(USER_HEADER, portalUser.username())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AssistantChatResponse.class);
    }

    public void deleteConversation(PortalUserSession portalUser, String conversationId) {
        restClient.delete()
                .uri("/api/assistant/conversations/{conversationId}", conversationId)
                .header(USER_HEADER, portalUser.username())
                .retrieve()
                .toBodilessEntity();
    }

    private String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}