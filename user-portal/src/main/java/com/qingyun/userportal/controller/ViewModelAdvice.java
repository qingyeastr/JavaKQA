package com.qingyun.userportal.controller;

import com.qingyun.userportal.web.PortalSessionConstants;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ViewModelAdvice {

    private final String adminBaseUrl;

    public ViewModelAdvice(@Value("${app.admin-base-url}") String adminBaseUrl) {
        this.adminBaseUrl = adminBaseUrl;
    }

    @ModelAttribute("portalUser")
    public Object portalUser(HttpSession session) {
        return session.getAttribute(PortalSessionConstants.PORTAL_USER);
    }

    @ModelAttribute("adminLoginUrl")
    public String adminLoginUrl() {
        return normalizeBaseUrl(adminBaseUrl) + "/login";
    }

    private String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}