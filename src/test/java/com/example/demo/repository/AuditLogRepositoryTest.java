package com.example.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.demo.entity.AuditLog;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuditLogRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
    }

    @Test
    void saveAuditLog_shouldSaveLogSuccessfully() {
        AuditLog log = new AuditLog();
        log.setUsername("admin");
        log.setAction("LOGIN");
        log.setModule("AUTH");
        log.setDescription("Admin user logged in");
        log.setActionTime(LocalDateTime.now());

        AuditLog savedLog = auditLogRepository.save(log);

        assertThat(savedLog.getId()).isNotNull();
        assertThat(savedLog.getUsername()).isEqualTo("admin");
        assertThat(savedLog.getAction()).isEqualTo("LOGIN");
        assertThat(savedLog.getModule()).isEqualTo("AUTH");
    }

    @Test
    void findById_shouldReturnAuditLog_whenExists() {
        AuditLog log = new AuditLog();
        log.setUsername("user1");
        log.setAction("CREATE_DONOR");
        log.setModule("DONOR");
        log.setActionTime(LocalDateTime.now());
        AuditLog savedLog = auditLogRepository.save(log);

        Optional<AuditLog> result = auditLogRepository.findById(savedLog.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("user1");
        assertThat(result.get().getAction()).isEqualTo("CREATE_DONOR");
    }

    @Test
    void findAll_shouldReturnAllAuditLogs() {
        AuditLog log1 = new AuditLog();
        log1.setUsername("user1");
        log1.setAction("LOGIN");
        log1.setModule("AUTH");
        log1.setActionTime(LocalDateTime.now());

        AuditLog log2 = new AuditLog();
        log2.setUsername("user2");
        log2.setAction("LOGOUT");
        log2.setModule("AUTH");
        log2.setActionTime(LocalDateTime.now());

        auditLogRepository.saveAll(List.of(log1, log2));

        List<AuditLog> logs = auditLogRepository.findAll();

        assertThat(logs).hasSize(2);
        assertThat(logs).extracting(AuditLog::getUsername).containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    void delete_shouldRemoveAuditLog() {
        AuditLog log = new AuditLog();
        log.setUsername("user1");
        log.setAction("DELETE");
        log.setModule("USER");
        log.setActionTime(LocalDateTime.now());
        AuditLog savedLog = auditLogRepository.save(log);

        auditLogRepository.delete(savedLog);

        Optional<AuditLog> result = auditLogRepository.findById(savedLog.getId());
        assertThat(result).isEmpty();
    }
}
