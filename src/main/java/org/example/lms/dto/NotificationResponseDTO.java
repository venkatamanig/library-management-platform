package org.example.lms.dto;

import java.time.LocalDateTime;

public record NotificationResponseDTO(
        Long id,
        String message,
        boolean read,
        LocalDateTime createdAt
) {}
