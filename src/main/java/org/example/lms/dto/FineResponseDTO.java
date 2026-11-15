
package org.example.lms.dto;

import org.example.lms.entity.FineStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FineResponseDTO(
        Long fineId,
        Long borrowingId,
        String bookTitle,
        BigDecimal amount,
        FineStatus status,
        LocalDateTime createdAt,
        LocalDateTime paidAt
) {}
