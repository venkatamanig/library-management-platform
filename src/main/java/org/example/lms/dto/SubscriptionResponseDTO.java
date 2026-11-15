package org.example.lms.dto;

import org.example.lms.entity.MembershipStatus;
import java.time.LocalDate;

public record SubscriptionResponseDTO(
        Long id,
        String patronEmail,
        String planName,
        LocalDate startDate,
        LocalDate endDate,
        MembershipStatus status
) {}
