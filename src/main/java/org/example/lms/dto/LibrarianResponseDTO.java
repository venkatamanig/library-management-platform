package org.example.lms.dto;

import org.example.lms.entity.AccountStatus;
import java.time.LocalDateTime;

public record LibrarianResponseDTO(
        Long id,
        String fullName,
        String email,
        AccountStatus status,
        LocalDateTime createdAt
) {}
