package org.example.lms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookCopyRequestDTO(
        @NotNull @Min(1) Integer copyNumber
) {}
