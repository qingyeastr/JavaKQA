package com.qingyun.userportal.controller;

import com.qingyun.userportal.service.PortalAuthService;
import com.qingyun.userportal.web.PortalLoginForm;
import com.qingyun.userportal.web.PortalSessionConstants;
import com.qingyun.userportal.web.PortalUserSession;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final PortalAuthService portalAuthService;

    public AuthController(PortalAuthService portalAuthService) {
        this.portalAuthService = portalAuthService;
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        if (session.getAttribute(PortalSessionConstants.PORTAL_USER) != null) {
            return "redirect:/";
        }
        if (!model.containsAttribute("portalLoginForm")) {
            model.addAttribute("portalLoginForm", new PortalLoginForm());
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("portalLoginForm") PortalLoginForm portalLoginForm,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        Optional<PortalUserSession> portalUser = portalAuthService.authenticate(
                portalLoginForm.getUsername(), portalLoginForm.getPassword());
        if (portalUser.isEmpty()) {
            model.addAttribute("errorMessage", "演示阶段密码固定为 123456，请重新输入后登录");
            return "login";
        }

        session.setAttribute(PortalSessionConstants.PORTAL_USER, portalUser.get());
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}