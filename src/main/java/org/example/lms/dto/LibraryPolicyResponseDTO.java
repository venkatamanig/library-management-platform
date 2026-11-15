package org.example.lms.dto;

import java.math.BigDecimal;

public record LibraryPolicyResponseDTO(
        Integer gracePeriodDays,
        BigDecimal finePerDay,
        BigDecimal maximumFine,
        boolean blockRenewalWithUnpaidFines
) {}
