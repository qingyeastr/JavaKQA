package com.qingyun.systemconfig.controller;

import com.qingyun.systemconfig.service.OperationLogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/logs")
public class LogController {

    private final OperationLogService operationLogService;

    public LogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping
    public String logs(Model model) {
        model.addAttribute("logs", operationLogService.listRecentLogs());
        return "logs";
    }
}