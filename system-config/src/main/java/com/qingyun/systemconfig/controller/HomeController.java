package com.qingyun.systemconfig.controller;

import com.qingyun.framework.web.SessionConstants;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session) {
        return session.getAttribute(SessionConstants.LOGIN_USER) == null ? "redirect:/login" : "redirect:/configs";
    }
}