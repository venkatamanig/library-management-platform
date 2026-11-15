package org.example.lms.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.example.lms.dto.AuditLogResponseDTO;
import org.example.lms.repository.AuditLogRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Audit", description = "Inspect administrative audit events.")
@RestController
@RequestMapping("/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponseDTO>> getAllLogs() {

        List<AuditLogResponseDTO> logs =
                auditLogRepository.findAllByOrderByPerformedAtDesc()
                        .stream()
                        .map(log -> new AuditLogResponseDTO(
                                log.getId(),
                                log.getPerformedBy(),
                                log.getAction(),
                                log.getDetails(),
                                log.getPerformedAt()
                        ))
                        .toList();

        return ResponseEntity.ok(logs);
    }
}
