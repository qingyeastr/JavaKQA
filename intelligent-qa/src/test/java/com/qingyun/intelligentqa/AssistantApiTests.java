package com.qingyun.intelligentqa;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AssistantApiTests {

    private static final String USER_HEADER = "X-Portal-User";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldExposeRuntimeConfig() throws Exception {
        mockMvc.perform(get("/api/assistant/runtime-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model.modelId").value("gpt-4.1"))
                .andExpect(jsonPath("$.retrieval.keywordTopK").value(10))
                .andExpect(jsonPath("$.prompt.referenceEnabled").value(true));
    }

    @Test
    void shouldCreateConversationAndListByUser() throws Exception {
        mockMvc.perform(post("/api/assistant/conversations")
                .header(USER_HEADER, "student01"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.conversationId").exists())
                .andExpect(jsonPath("$.title").value("新对话"));

        mockMvc.perform(get("/api/assistant/conversations")
                .header(USER_HEADER, "student01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].conversationId").exists())
                .andExpect(jsonPath("$[0].title").exists());
    }

    @Test
    void shouldReturnPlaceholderChatAndPersistMessages() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/assistant/chat")
                .header(USER_HEADER, "student01")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          \"message\": \"Spring Boot 的自动配置为什么可以按需生效？\"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").exists())
                .andExpect(jsonPath("$.status").value("PENDING_IMPLEMENTATION"))
                .andExpect(jsonPath("$.citations[0].documentTitle").exists())
                .andReturn();

        String conversationId = JsonPath.read(result.getResponse().getContentAsString(), "$.conversationId");

        mockMvc.perform(get("/api/assistant/conversations/{conversationId}/messages", conversationId)
                .header(USER_HEADER, "student01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(conversationId))
                .andExpect(jsonPath("$.messages[0].role").value("user"))
                .andExpect(jsonPath("$.messages[1].role").value("assistant"))
                .andExpect(jsonPath("$.messages[1].citations[0].sourceUrl").exists());
    }
}