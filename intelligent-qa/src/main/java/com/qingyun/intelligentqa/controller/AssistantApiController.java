package com.qingyun.intelligentqa.controller;

import com.qingyun.intelligentqa.api.assistant.AssistantChatRequest;
import com.qingyun.intelligentqa.api.assistant.AssistantChatResponse;
import com.qingyun.intelligentqa.api.assistant.AssistantConversationCreatedResponse;
import com.qingyun.intelligentqa.api.assistant.AssistantConversationMessagesResponse;
import com.qingyun.intelligentqa.api.assistant.AssistantConversationSummaryResponse;
import com.qingyun.intelligentqa.api.assistant.AssistantHealthResponse;
import com.qingyun.intelligentqa.api.assistant.AssistantRuntimeConfigResponse;
import com.qingyun.intelligentqa.service.AssistantService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
public class AssistantApiController {

    public static final String USER_HEADER = "X-Portal-User";

    private final AssistantService assistantService;

    public AssistantApiController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @GetMapping("/health")
    public AssistantHealthResponse health() {
        return assistantService.health();
    }

    @GetMapping("/runtime-config")
    public AssistantRuntimeConfigResponse runtimeConfig() {
        return assistantService.runtimeConfig();
    }

    @PostMapping("/conversations")
    @ResponseStatus(HttpStatus.CREATED)
    public AssistantConversationCreatedResponse createConversation(
            @RequestHeader(name = USER_HEADER, required = false) String userId) {
        return assistantService.createConversation(userId);
    }

    @GetMapping("/conversations")
    public List<AssistantConversationSummaryResponse> conversations(
            @RequestHeader(name = USER_HEADER, required = false) String userId) {
        return assistantService.listConversations(userId);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public AssistantConversationMessagesResponse conversationMessages(
            @RequestHeader(name = USER_HEADER, required = false) String userId,
            @PathVariable String conversationId) {
        return assistantService.conversationMessages(userId, conversationId);
    }

    @DeleteMapping("/conversations/{conversationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConversation(
            @RequestHeader(name = USER_HEADER, required = false) String userId,
            @PathVariable String conversationId) {
        assistantService.deleteConversation(userId, conversationId);
    }

    @PostMapping("/chat")
    public AssistantChatResponse chat(
            @RequestHeader(name = USER_HEADER, required = false) String userId,
            @Valid @RequestBody AssistantChatRequest request) {
        return assistantService.chat(userId, request);
    }
}