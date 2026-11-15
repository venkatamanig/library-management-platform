package org.example.lms.service;

import org.example.lms.entity.AuditLog;
import org.example.lms.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(
            String performedBy,
            String action,
            String details) {

        AuditLog auditLog = new AuditLog();

        auditLog.setPerformedBy(performedBy);
        auditLog.setAction(action);
        auditLog.setDetails(details);

        auditLogRepository.save(auditLog);
    }

    public void logCurrentUser(String action, String details) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = authentication == null ? "SYSTEM" : authentication.getName();
        log(actor, action, details);
    }
}
