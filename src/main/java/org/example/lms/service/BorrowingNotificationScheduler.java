
package org.example.lms.service;

import org.example.lms.entity.Borrowing;
import org.example.lms.entity.BorrowingStatus;
import org.example.lms.repository.BorrowingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@ConditionalOnProperty(name = "lms.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class BorrowingNotificationScheduler {

    private final BorrowingRepository borrowingRepository;
    private final NotificationService notificationService;

    public BorrowingNotificationScheduler(
            BorrowingRepository borrowingRepository,
            NotificationService notificationService) {
        this.borrowingRepository = borrowingRepository;
        this.notificationService = notificationService;
    }

    // Runs every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void sendBorrowingNotifications() {

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

        // 1. Notify patrons about books due within 24 hours
        List<Borrowing> dueSoon =
                borrowingRepository
                        .findByStatusAndDueAtBetweenAndDueReminderSentFalse(
                                BorrowingStatus.ACTIVE,
                                now,
                                now.plusDays(1)
                        );

        for (Borrowing borrowing : dueSoon) {

            String email = borrowing.getPatron().getEmail();
            String title = borrowing.getBookCopy()
                    .getBook().getTitle();

            notificationService.sendNotification(
                    email,
                    "Reminder: Your book '" + title
                            + "' is due on "
                            + borrowing.getDueAt().toLocalDate()
            );

            borrowing.setDueReminderSent(true);
        }

        // 2. Notify patrons about overdue books
        List<Borrowing> overdue =
                borrowingRepository
                        .findByStatusAndOverdueNotificationSentFalse(
                                BorrowingStatus.OVERDUE
                        );

        for (Borrowing borrowing : overdue) {

            String email = borrowing.getPatron().getEmail();
            String title = borrowing.getBookCopy()
                    .getBook().getTitle();

            notificationService.sendNotification(
                    email,
                    "Overdue notice: Your book '" + title
                            + "' is overdue. Please return it as soon as possible."
            );

            borrowing.setOverdueNotificationSent(true);
        }
    }
}
