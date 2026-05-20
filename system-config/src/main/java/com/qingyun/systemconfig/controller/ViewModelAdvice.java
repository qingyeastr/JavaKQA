package com.qingyun.systemconfig.controller;

import com.qingyun.framework.web.SessionConstants;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ViewModelAdvice {

    private final String portalBaseUrl;

    public ViewModelAdvice(@Value("${app.portal-base-url}") String portalBaseUrl) {
        this.portalBaseUrl = portalBaseUrl;
    }

    @ModelAttribute("currentUser")
    public Object currentUser(HttpSession session) {
        return session.getAttribute(SessionConstants.LOGIN_USER);
    }

    @ModelAttribute("portalLoginUrl")
    public String portalLoginUrl() {
        return normalizeBaseUrl(portalBaseUrl) + "/login";
    }

    private String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}