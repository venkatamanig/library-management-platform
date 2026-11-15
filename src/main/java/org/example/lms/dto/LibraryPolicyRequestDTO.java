package org.example.lms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record LibraryPolicyRequestDTO(
        @NotNull @Min(0) Integer gracePeriodDays,
        @NotNull @DecimalMin("0.01") BigDecimal finePerDay,
        @NotNull @DecimalMin("0.01") BigDecimal maximumFine,
        boolean blockRenewalWithUnpaidFines
) {}
