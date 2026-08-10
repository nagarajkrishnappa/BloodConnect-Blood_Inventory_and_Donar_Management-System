package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.response.AuditLogResponse;

public interface AuditLogService {

    void saveLog(
            String username,
            String action,
            String module,
            String description);

    List<AuditLogResponse> getAllLogs();

}