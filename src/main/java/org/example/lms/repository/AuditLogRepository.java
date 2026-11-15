package org.example.lms.repository;

import org.example.lms.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByPerformedAtDesc();

    List<AuditLog> findByPerformedByOrderByPerformedAtDesc(
            String performedBy
    );
}
