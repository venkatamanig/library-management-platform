package org.example.lms.dto;

public record MembershipPlanResponseDTO(
        Long id,
        String name,
        Integer durationDays,
        Integer loanPeriodDays,
        Integer maxBooks,
        Integer maxRenewals,
        boolean active
) implements java.io.Serializable {}
