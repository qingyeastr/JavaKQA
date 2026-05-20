package com.qingyun.systemconfig.controller;

import com.qingyun.framework.security.LoginUser;
import com.qingyun.framework.web.SessionConstants;
import com.qingyun.systemconfig.service.UserService;
import com.qingyun.systemconfig.web.UserForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String users(Model model) {
        preparePage(model);
        return "users";
    }

    @PostMapping
    public String createUser(@Valid @ModelAttribute("userForm") UserForm userForm,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            preparePage(model);
            model.addAttribute("formError", "请完整填写用户信息");
            return "users";
        }

        try {
            userService.createUser(userForm, currentUser(session));
            redirectAttributes.addFlashAttribute("message", "管理员账号已创建");
            return "redirect:/users";
        } catch (IllegalArgumentException exception) {
            preparePage(model);
            model.addAttribute("formError", exception.getMessage());
            return "users";
        }
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
            @RequestParam short status,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        userService.updateStatus(id, status, currentUser(session));
        redirectAttributes.addFlashAttribute("message", status == 1 ? "账号已启用" : "账号已停用");
        return "redirect:/users";
    }

    private void preparePage(Model model) {
        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new UserForm());
        }
        model.addAttribute("users", userService.listUsers());
    }

    private LoginUser currentUser(HttpSession session) {
        return (LoginUser) session.getAttribute(SessionConstants.LOGIN_USER);
    }
}