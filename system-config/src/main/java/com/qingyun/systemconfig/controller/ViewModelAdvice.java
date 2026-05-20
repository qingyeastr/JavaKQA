package com.qingyun.systemconfig.controller;

import com.qingyun.framework.web.SessionConstants;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ViewModelAdvice {

    @ModelAttribute("currentUser")
    public Object currentUser(HttpSession session) {
        return session.getAttribute(SessionConstants.LOGIN_USER);
    }
}