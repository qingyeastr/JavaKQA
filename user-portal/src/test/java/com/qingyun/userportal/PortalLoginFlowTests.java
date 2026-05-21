package com.qingyun.userportal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class PortalLoginFlowTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldRenderLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("前台体验入口")))
                .andExpect(content().string(containsString("管理员登录")));
    }

    @Test
    void shouldProtectOverviewWithoutLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void shouldLoginAndRenderOverview() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
                .param("username", "student01")
                .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("智能问答")))
                .andExpect(content().string(containsString("知识库管理")))
                .andExpect(content().string(containsString("管理员登录")));
    }

    @Test
    void shouldRenderModulePageAfterLogin() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
                .param("username", "student01")
                .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        mockMvc.perform(get("/analytics").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("数据分析")))
                .andExpect(content().string(containsString("今日问答量")));
    }

    @Test
    void shouldRenderQaModuleAfterLogin() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
                .param("username", "student01")
                .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        mockMvc.perform(get("/qa").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("对话列表")))
                .andExpect(content().string(containsString("新建对话")))
                .andExpect(content().string(containsString("独立 intelligent-qa 模块")));
    }
}