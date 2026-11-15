package org.example.lms.dto;

import org.example.lms.entity.BorrowingStatus;
import java.time.LocalDateTime;

public record BorrowingResponseDTO(
        Long borrowingId,
        String bookTitle,
        Integer copyNumber,
        LocalDateTime borrowedAt,
        LocalDateTime dueAt,
        BorrowingStatus status
) {}
