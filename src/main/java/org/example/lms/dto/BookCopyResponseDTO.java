package org.example.lms.dto;

import org.example.lms.entity.BookCopyStatus;

public record BookCopyResponseDTO(
        Long id,
        Long bookId,
        String bookTitle,
        Integer copyNumber,
        BookCopyStatus status
) {}
