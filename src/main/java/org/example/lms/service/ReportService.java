
package org.example.lms.service;

import org.example.lms.dto.DashboardResponseDTO;
import org.example.lms.dto.BorrowingResponseDTO;
import org.example.lms.entity.*;
import org.example.lms.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ReportService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BorrowingRepository borrowingRepository;
    private final UserRepository userRepository;
    private final FineRepository fineRepository;

    public ReportService(
            BookRepository bookRepository,
            BookCopyRepository bookCopyRepository,
            BorrowingRepository borrowingRepository,
            UserRepository userRepository,
            FineRepository fineRepository) {

        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.borrowingRepository = borrowingRepository;
        this.userRepository = userRepository;
        this.fineRepository = fineRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponseDTO getDashboard() {

        return new DashboardResponseDTO(
                bookRepository.count(),
                bookCopyRepository.count(),
                bookCopyRepository.countByStatus(BookCopyStatus.AVAILABLE),
                bookCopyRepository.countByStatus(BookCopyStatus.BORROWED),
                userRepository.countByRole(Role.PATRON),
                userRepository.countByRoleAndStatus(
                        Role.PATRON, AccountStatus.PENDING),
                borrowingRepository.countByStatus(BorrowingStatus.ACTIVE),
                borrowingRepository.countByStatus(BorrowingStatus.OVERDUE),
                fineRepository.count(),
                fineRepository.countByStatus(FineStatus.UNPAID),
                fineRepository.sumAllFineAmounts(),
                fineRepository.sumFineAmountsByStatus(FineStatus.UNPAID)
        );
    }

    @Transactional(readOnly = true)
    public List<BorrowingResponseDTO> getAllBorrowings() {

        return borrowingRepository.findAllByOrderByBorrowedAtDesc()
                .stream()
                .map(b -> new BorrowingResponseDTO(
                        b.getId(),
                        b.getBookCopy().getBook().getTitle(),
                        b.getBookCopy().getCopyNumber(),
                        b.getBorrowedAt(),
                        b.getDueAt(),
                        b.getStatus()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BorrowingResponseDTO> getOverdueBorrowings() {

        return borrowingRepository.findByStatus(BorrowingStatus.OVERDUE)
                .stream()
                .map(b -> new BorrowingResponseDTO(
                        b.getId(),
                        b.getBookCopy().getBook().getTitle(),
                        b.getBookCopy().getCopyNumber(),
                        b.getBorrowedAt(),
                        b.getDueAt(),
                        b.getStatus()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getFineReport() {

        return java.util.Map.of(
                "totalFines", fineRepository.count(),
                "paidFines", fineRepository.countByStatus(FineStatus.PAID),
                "unpaidFines", fineRepository.countByStatus(FineStatus.UNPAID),
                "totalAmount", fineRepository.sumAllFineAmounts(),
                "unpaidAmount",
                fineRepository.sumFineAmountsByStatus(FineStatus.UNPAID)
        );
    }
}
