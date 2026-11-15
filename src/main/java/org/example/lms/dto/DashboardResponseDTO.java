
package org.example.lms.dto;

import java.math.BigDecimal;

public record DashboardResponseDTO(
        long totalBooks,
        long totalCopies,
        long availableCopies,
        long borrowedCopies,
        long totalPatrons,
        long pendingPatrons,
        long activeBorrowings,
        long overdueBorrowings,
        long totalFines,
        long unpaidFines,
        BigDecimal totalFineAmount,
        BigDecimal unpaidFineAmount
) {}
