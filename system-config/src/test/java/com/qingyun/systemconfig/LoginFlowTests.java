package com.qingyun.systemconfig;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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
class LoginFlowTests {

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
                .andExpect(content().string(containsString("系统配置模块")));
    }

    @Test
    void shouldLoginWithDefaultAdmin() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "admin")
                .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/configs"));
    }

    @Test
    void shouldRenderFixedConfigPageAfterLogin() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
                .param("username", "admin")
                .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        mockMvc.perform(get("/configs").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("模型配置")))
                .andExpect(content().string(containsString("参数定位")))
                .andExpect(content().string(containsString("大语言模型配置")))
                .andExpect(content().string(not(containsString("自定义模型列表"))))
                .andExpect(content().string(not(containsString("前台助手接口"))));
    }
}