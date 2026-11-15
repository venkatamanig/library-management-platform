package org.example.lms.dto;

import jakarta.validation.constraints.NotNull;
import org.example.lms.entity.BookCopyStatus;

public record BookCopyStatusUpdateDTO(
        @NotNull BookCopyStatus status
) {}
