package com.example.demo.dto.response;

import java.time.LocalDateTime;

public class AuditLogResponse {

    private Long id;
    private String username;
    private String action;
    private String module;
    private String description;
    private LocalDateTime actionTime;

    public AuditLogResponse() {
    }

    public AuditLogResponse(Long id, String username, String action,
            String module, String description,
            LocalDateTime actionTime) {
        this.id = id;
        this.username = username;
        this.action = action;
        this.module = module;
        this.description = description;
        this.actionTime = actionTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getActionTime() {
        return actionTime;
    }

    public void setActionTime(LocalDateTime actionTime) {
        this.actionTime = actionTime;
    }
}