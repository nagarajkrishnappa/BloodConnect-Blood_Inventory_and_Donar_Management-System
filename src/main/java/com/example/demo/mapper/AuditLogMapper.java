package com.example.demo.mapper;

import com.example.demo.dto.response.AuditLogResponse;
import com.example.demo.entity.AuditLog;

public class AuditLogMapper {

    public static AuditLogResponse toResponse(AuditLog auditLog) {

        AuditLogResponse response = new AuditLogResponse();

        response.setId(auditLog.getId());
        response.setUsername(auditLog.getUsername());
        response.setAction(auditLog.getAction());
        response.setModule(auditLog.getModule());
        response.setDescription(auditLog.getDescription());
        response.setActionTime(auditLog.getActionTime());

        return response;
    }
}