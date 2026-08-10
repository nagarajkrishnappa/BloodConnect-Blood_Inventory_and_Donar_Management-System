package com.example.demo.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.response.AuditLogResponse;
import com.example.demo.entity.AuditLog;
import com.example.demo.mapper.AuditLogMapper;
import com.example.demo.repository.AuditLogRepository;
import com.example.demo.service.AuditLogService;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    public void saveLog(
            String username,
            String action,
            String module,
            String description) {

        AuditLog log = new AuditLog();

        log.setUsername(username);
        log.setAction(action);
        log.setModule(module);
        log.setDescription(description);
        log.setActionTime(LocalDateTime.now());

        auditLogRepository.save(log);
    }

    @Override
    public List<AuditLogResponse> getAllLogs() {

        return auditLogRepository.findAll()
                .stream()
                .map(AuditLogMapper::toResponse)
                .collect(Collectors.toList());
    }
}