package com.qingyun.userportal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.qingyun.userportal.api.assistant.AssistantChatResponse;
import com.qingyun.userportal.api.assistant.AssistantConversationSummaryResponse;
import com.qingyun.userportal.api.assistant.AssistantRuntimeConfigResponse;
import com.qingyun.userportal.service.AssistantGatewayService;
import com.qingyun.userportal.web.PortalSessionConstants;
import com.qingyun.userportal.web.PortalUserSession;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AssistantProxyApiTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private AssistantGatewayService assistantGatewayService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldRequireLoginForAssistantProxyApi() throws Exception {
        mockMvc.perform(get("/api/assistant/conversations"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void shouldReturnConversationListThroughProxy() throws Exception {
        when(assistantGatewayService.listConversations(any(PortalUserSession.class)))
                .thenReturn(List.of(new AssistantConversationSummaryResponse(
                        "conversation-1",
                        "Spring Boot 自动配置",
                        "占位答案已返回",
                        "ACTIVE",
                        LocalDateTime.now(),
                        LocalDateTime.now())));

        mockMvc.perform(get("/api/assistant/conversations").session(authenticatedSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].conversationId").value("conversation-1"))
                .andExpect(jsonPath("$[0].title").value("Spring Boot 自动配置"));
    }

    @Test
    void shouldReturnChatResponseThroughProxy() throws Exception {
        AssistantRuntimeConfigResponse runtimeConfig = new AssistantRuntimeConfigResponse(
                new AssistantRuntimeConfigResponse.ModelConfig("gpt-4.1", "https://api.openai.com/v1", 30000,
                        "text-embedding-3-large", 3072),
                new AssistantRuntimeConfigResponse.RetrievalConfig(10, 8, 0.6, true, 10, 0.72, 4),
                new AssistantRuntimeConfigResponse.PromptConfig("system", true, 6, 1200, true),
                new AssistantRuntimeConfigResponse.SystemSwitchConfig(true, true, true, false));

        when(assistantGatewayService.chat(any(PortalUserSession.class), any()))
                .thenReturn(new AssistantChatResponse(
                        "conversation-1",
                        "user-message-1",
                        "assistant-message-1",
                        "PENDING_IMPLEMENTATION",
                        "这是第一版占位答案。",
                        List.of(),
                        List.of("继续追问：AutoConfiguration.imports 是如何被加载的？"),
                        runtimeConfig,
                        LocalDateTime.now()));

        mockMvc.perform(post("/api/assistant/chat")
                .session(authenticatedSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          \"conversationId\": \"conversation-1\",
                          \"message\": \"Spring Boot 的自动配置为什么可以按需生效？\"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value("conversation-1"))
                .andExpect(jsonPath("$.status").value("PENDING_IMPLEMENTATION"))
                .andExpect(jsonPath("$.runtimeConfig.model.modelId").value("gpt-4.1"));
    }

    private MockHttpSession authenticatedSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(PortalSessionConstants.PORTAL_USER, new PortalUserSession("student01", "student01"));
        return session;
    }
}