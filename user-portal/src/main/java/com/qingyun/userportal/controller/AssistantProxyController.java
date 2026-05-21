package com.qingyun.userportal.controller;

import com.qingyun.userportal.api.assistant.AssistantApiErrorResponse;
import com.qingyun.userportal.api.assistant.AssistantChatRequest;
import com.qingyun.userportal.service.AssistantGatewayService;
import com.qingyun.userportal.web.PortalSessionConstants;
import com.qingyun.userportal.web.PortalUserSession;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/assistant")
public class AssistantProxyController {

    private final AssistantGatewayService assistantGatewayService;

    public AssistantProxyController(AssistantGatewayService assistantGatewayService) {
        this.assistantGatewayService = assistantGatewayService;
    }

    @GetMapping("/runtime-config")
    public ResponseEntity<?> runtimeConfig(HttpSession session) {
        PortalUserSession portalUser = currentUser(session);
        try {
            return ResponseEntity.ok(assistantGatewayService.runtimeConfig(portalUser));
        } catch (RestClientException exception) {
            return gatewayError(exception);
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> conversations(HttpSession session) {
        PortalUserSession portalUser = currentUser(session);
        try {
            return ResponseEntity.ok(assistantGatewayService.listConversations(portalUser));
        } catch (RestClientException exception) {
            return gatewayError(exception);
        }
    }

    @PostMapping("/conversations")
    public ResponseEntity<?> createConversation(HttpSession session) {
        PortalUserSession portalUser = currentUser(session);
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(assistantGatewayService.createConversation(portalUser));
        } catch (RestClientException exception) {
            return gatewayError(exception);
        }
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<?> conversationMessages(@PathVariable String conversationId, HttpSession session) {
        PortalUserSession portalUser = currentUser(session);
        try {
            return ResponseEntity.ok(assistantGatewayService.conversationMessages(portalUser, conversationId));
        } catch (RestClientException exception) {
            return gatewayError(exception);
        }
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<?> deleteConversation(@PathVariable String conversationId, HttpSession session) {
        PortalUserSession portalUser = currentUser(session);
        try {
            assistantGatewayService.deleteConversation(portalUser, conversationId);
            return ResponseEntity.noContent().build();
        } catch (RestClientException exception) {
            return gatewayError(exception);
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@Valid @RequestBody AssistantChatRequest request, HttpSession session) {
        PortalUserSession portalUser = currentUser(session);
        try {
            return ResponseEntity.ok(assistantGatewayService.chat(portalUser, request));
        } catch (RestClientException exception) {
            return gatewayError(exception);
        }
    }

    private PortalUserSession currentUser(HttpSession session) {
        Object value = session.getAttribute(PortalSessionConstants.PORTAL_USER);
        if (value instanceof PortalUserSession portalUser) {
            return portalUser;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已失效");
    }

    private ResponseEntity<AssistantApiErrorResponse> gatewayError(RestClientException exception) {
        if (exception instanceof RestClientResponseException responseException) {
            HttpStatusCode statusCode = responseException.getStatusCode();
            return ResponseEntity.status(statusCode)
                    .body(new AssistantApiErrorResponse(
                            "INTELLIGENT_QA_ERROR",
                            "智能问答模块返回异常状态：" + statusCode.value()));
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new AssistantApiErrorResponse(
                        "INTELLIGENT_QA_UNAVAILABLE",
                        "智能问答模块暂时不可用，请确认 intelligent-qa 服务已启动。"));
    }
}