package org.example.lms.dto;

import java.math.BigDecimal;
import java.util.List;

public record PatronDetailsResponseDTO(
        PatronResponseDTO patron,
        List<BorrowingResponseDTO> borrowings,
        List<FineResponseDTO> fines,
        List<SubscriptionResponseDTO> subscriptions,
        BigDecimal outstandingBalance
) {}
