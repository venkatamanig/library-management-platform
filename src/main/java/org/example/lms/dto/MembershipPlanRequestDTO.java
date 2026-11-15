
package org.example.lms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MembershipPlanRequestDTO(
        @NotBlank String name,
        @NotNull @Min(1) Integer durationDays,
        @NotNull @Min(1) Integer loanPeriodDays,
        @NotNull @Min(1) Integer maxBooks,
        @NotNull @Min(0) Integer maxRenewals
) {}
