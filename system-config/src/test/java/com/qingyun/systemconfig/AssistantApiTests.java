package com.qingyun.systemconfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AssistantApiTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldExposeAssistantRuntimeConfigWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/assistant/runtime-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model.modelId").value("gpt-4.1"))
                .andExpect(jsonPath("$.model.apiBaseUrl").exists())
                .andExpect(jsonPath("$.retrieval.keywordTopK").exists())
                .andExpect(jsonPath("$.prompt.systemTemplate").exists())
                .andExpect(jsonPath("$.system.feedbackEnabled").exists());
    }

    @Test
    void shouldReturnPlaceholderChatResponseWithoutLogin() throws Exception {
        mockMvc.perform(post("/api/assistant/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "sessionId": "demo-session",
                          "message": "Spring Boot 的自动配置原理是什么？"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("demo-session"))
                .andExpect(jsonPath("$.status").value("PENDING_IMPLEMENTATION"))
                .andExpect(jsonPath("$.runtimeConfig.model.modelId").exists());
    }
}