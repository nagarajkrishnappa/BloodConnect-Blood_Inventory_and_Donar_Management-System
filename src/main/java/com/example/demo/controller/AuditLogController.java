package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.service.AuditLogService;

@Controller
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/admin/auditlogs")
    public String viewAuditLogs(Model model) {

        model.addAttribute(
                "auditLogs",
                auditLogService.getAllLogs());

        return "admin/audit/list";
    }
}