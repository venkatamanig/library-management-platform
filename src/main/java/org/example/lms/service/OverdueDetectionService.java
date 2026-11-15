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
public class OverdueDetectionService {

    private final BorrowingRepository borrowingRepository;

    public OverdueDetectionService(
            BorrowingRepository borrowingRepository) {
        this.borrowingRepository = borrowingRepository;
    }

//    @Scheduled(cron = "0 0 1 * * *")
//    @Scheduled(fixedRate = 60000)
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void detectOverdueBorrowings() {

        List<Borrowing> borrowings =
                borrowingRepository.findByStatusAndDueAtBefore(
                        BorrowingStatus.ACTIVE,
                        LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
                );

        for (Borrowing borrowing : borrowings) {
            borrowing.setStatus(BorrowingStatus.OVERDUE);
        }

        System.out.println(
                "Overdue borrowings detected: " + borrowings.size()
        );
    }
}
