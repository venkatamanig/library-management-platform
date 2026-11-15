
package org.example.lms.repository;

import org.example.lms.entity.Borrowing;
import org.example.lms.entity.BorrowingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BorrowingRepository
        extends JpaRepository<Borrowing, Long> {

    List<Borrowing> findByPatron_EmailOrderByBorrowedAtDesc(
            String email
    );

    Optional<Borrowing> findByIdAndPatron_Email(
            Long id, String email
    );

    List<Borrowing> findByStatus(BorrowingStatus status);

    List<Borrowing> findByStatusAndDueAtBefore(
            BorrowingStatus status,
            LocalDateTime dateTime
    );

    long countByStatus(BorrowingStatus status);

    List<Borrowing> findAllByOrderByBorrowedAtDesc();

    List<Borrowing> findByStatusAndDueAtBetweenAndDueReminderSentFalse(
            BorrowingStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Borrowing> findByStatusAndOverdueNotificationSentFalse(
            BorrowingStatus status
    );

    long countByPatron_EmailAndStatusIn(
            String email,
            List<BorrowingStatus> statuses
    );

    List<Borrowing> findByPatron_IdOrderByBorrowedAtDesc(Long patronId);

    boolean existsByBookCopy_Id(Long copyId);
}
