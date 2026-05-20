package com.qingyun.systemconfig.controller;

import com.qingyun.framework.security.LoginUser;
import com.qingyun.framework.web.SessionConstants;
import com.qingyun.systemconfig.service.AuthService;
import com.qingyun.systemconfig.web.LoginForm;
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

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        if (session.getAttribute(SessionConstants.LOGIN_USER) != null) {
            return "redirect:/configs";
        }
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm loginForm,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        Optional<LoginUser> loginUser = authService.authenticate(loginForm.getUsername(), loginForm.getPassword());
        if (loginUser.isEmpty()) {
            model.addAttribute("errorMessage", "用户名或密码错误，或账号已被停用");
            return "login";
        }

        session.setAttribute(SessionConstants.LOGIN_USER, loginUser.get());
        return "redirect:/configs";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute(SessionConstants.LOGIN_USER);
        if (loginUser != null) {
            authService.recordLogout(loginUser);
        }
        session.invalidate();
        return "redirect:/login";
    }
}