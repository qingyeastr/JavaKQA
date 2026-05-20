package com.qingyun.userportal.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    private static final List<PortalModule> MODULES = List.of(
            new PortalModule("overview", "产品概览", "总览", "查看 JavaKQA 从文档建设到问答闭环的整体前台体验。"),
            new PortalModule("qa", "智能问答", "问答中心", "面向普通用户的提问入口，支持引用来源、多轮追问和答案总结。"),
            new PortalModule("knowledge", "知识库管理", "文档入库", "聚焦文档导入、解析、切片和知识来源巡检等前台操作视图。"),
            new PortalModule("retrieval", "文档检索", "召回调度", "展示关键词检索、语义检索与混合召回的协同检索体验。"),
            new PortalModule("conversation", "对话管理", "会话上下文", "集中查看历史会话、上下文记忆和连续追问的整理效果。"),
            new PortalModule("feedback", "反馈管理", "反馈闭环", "承接点赞点踩、纠错补充与知识缺口回流的产品前台。"),
            new PortalModule("analytics", "数据分析", "运行分析", "用前台可视化视角呈现问答量、热点主题和未命中问题。"),
            new PortalModule("settings", "系统配置", "运行参数", "展示与前台体验相关的模型、检索和界面策略信息。"));

    @GetMapping("/")
    public String overview(Model model) {
        preparePage(model, MODULES.get(0));
        return "portal";
    }

    @GetMapping("/{moduleKey}")
    public String modulePage(@PathVariable String moduleKey, Model model) {
        Optional<PortalModule> targetModule = MODULES.stream()
                .filter(module -> module.key().equals(moduleKey))
                .findFirst();
        if (targetModule.isEmpty()) {
            return "redirect:/";
        }
        preparePage(model, targetModule.get());
        return "portal";
    }

    private void preparePage(Model model, PortalModule activeModule) {
        model.addAttribute("activePortalModule", activeModule);
        model.addAttribute("portalModules", MODULES);
    }

    private record PortalModule(String key, String label, String badge, String description) {
    }
}