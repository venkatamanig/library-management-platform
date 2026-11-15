package org.example.lms.dto;

import java.time.LocalDateTime;

public record AuditLogResponseDTO(
        Long id,
        String performedBy,
        String action,
        String details,
        LocalDateTime performedAt
) {}
