
package org.example.lms.service;

import org.example.lms.dto.BorrowingResponseDTO;

import org.example.lms.entity.*;
import org.example.lms.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

@Service
public class BorrowingService {

    private final UserRepository userRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BorrowingRepository borrowingRepository;
    private final FineRepository fineRepository;
    private final PatronSubscriptionRepository subscriptionRepository;
    private final LibraryPolicyRepository policyRepository;

    private final NotificationService notificationService;
    private final AuditService auditService;

    public BorrowingService(
            UserRepository userRepository,
            BookCopyRepository bookCopyRepository,
            BorrowingRepository borrowingRepository,
            FineRepository fineRepository,
            AuditService auditService,
            NotificationService notificationService,
            PatronSubscriptionRepository subscriptionRepository,
            LibraryPolicyRepository policyRepository) {
        this.userRepository = userRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.borrowingRepository = borrowingRepository;
        this.fineRepository = fineRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.subscriptionRepository = subscriptionRepository;
        this.policyRepository = policyRepository;
    }

    @Transactional
    public BorrowingResponseDTO borrowBook(Long copyId, String email) {

        // 1. Find patron
        AppUser patron = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Patron not found"));

        // 2. Check account status
        if (patron.getRole() != Role.PATRON ||
                patron.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only active patrons can borrow books");
        }

        PatronSubscription subscription = getActiveSubscription(email);

        long currentBorrowings =
                borrowingRepository.countByPatron_EmailAndStatusIn(
                        email,
                        Arrays.asList(
                                BorrowingStatus.ACTIVE,
                                BorrowingStatus.OVERDUE
                        )
                );

        if (currentBorrowings >= subscription.getPlan().getMaxBooks()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You have reached your membership borrowing limit"
            );
        }

        // 3. Find and lock physical copy
        BookCopy copy = bookCopyRepository.findByIdForUpdate(copyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Book copy not found"));

        // 4. Check availability
        if (copy.getStatus() != BookCopyStatus.AVAILABLE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This book copy is not available");
        }

        // 5. Create borrowing record
        Borrowing borrowing = new Borrowing();
        borrowing.setPatron(patron);
        borrowing.setBookCopy(copy);
        borrowing.setDueAt(
                LocalDateTime.now(ZoneId.of("Asia/Kolkata")).plusDays(
                        subscription.getPlan().getLoanPeriodDays()
                )
        );
        borrowing.setStatus(BorrowingStatus.ACTIVE);

        // 6. Update physical copy status
        copy.setStatus(BookCopyStatus.BORROWED);

        // 7. Save
        Borrowing saved = borrowingRepository.save(borrowing);

        notificationService.sendNotification(
                email,
                "You have successfully borrowed "
                        + copy.getBook().getTitle()
                        + ". Due date: " + saved.getDueAt()
        );

        auditService.log(
                email,
                "BOOK_BORROWED",
                "Borrowing ID: " + saved.getId()
        );

        return new BorrowingResponseDTO(
                saved.getId(),
                copy.getBook().getTitle(),
                copy.getCopyNumber(),
                saved.getBorrowedAt(),
                saved.getDueAt(),
                saved.getStatus()
        );
    }


    @Transactional(readOnly = true)
    public List<BorrowingResponseDTO> getMyBorrowings(String email) {

        return borrowingRepository
                .findByPatron_EmailOrderByBorrowedAtDesc(email)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BorrowingResponseDTO renewBorrowing(Long id, String email) {

        Borrowing borrowing = borrowingRepository
                .findByIdAndPatron_Email(id, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Borrowing record not found"));

        if (borrowing.getStatus() != BorrowingStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only active borrowings can be renewed");
        }

        LibraryPolicy policy = policyRepository.findById(1L)
                .orElseGet(LibraryPolicy::new);

        if (policy.isBlockRenewalWithUnpaidFines()) {

            long unpaidFines =
                    fineRepository.countByBorrowing_Patron_EmailAndStatus(
                            email,
                            FineStatus.UNPAID
                    );

            if (unpaidFines > 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Please clear your unpaid fines before renewing books"
                );
            }
        }

        PatronSubscription subscription = getActiveSubscription(email);

        if (borrowing.getRenewalCount()
                >= subscription.getPlan().getMaxRenewals()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You have reached your membership renewal limit"
            );
        }

        if (borrowing.getDueAt().isBefore(LocalDateTime.now(ZoneId.of("Asia/Kolkata")))) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Overdue books cannot be renewed");
        }



        borrowing.setDueAt(
                borrowing.getDueAt().plusDays(
                        subscription.getPlan().getLoanPeriodDays()
                )
        );
        borrowing.setRenewalCount(borrowing.getRenewalCount() + 1);
        borrowing.setDueReminderSent(false);

        notificationService.sendNotification(
                email,
                "Your book renewal was successful. New due date: "
                        + borrowing.getDueAt()
        );

        return toResponse(borrowing);
    }


    @Transactional
    public BorrowingResponseDTO returnBook(Long id, String email) {

        Borrowing borrowing = borrowingRepository
                .findByIdAndPatron_Email(id, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Borrowing record not found"
                ));

        if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This book has already been returned."
            );
        }

        LocalDateTime returnedAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

        borrowing.setStatus(BorrowingStatus.RETURNED);
        borrowing.setReturnedAt(returnedAt);


        LibraryPolicy policy = policyRepository.findById(1L)
                .orElseGet(LibraryPolicy::new);

        LocalDate dueDate = borrowing.getDueAt().toLocalDate();
        LocalDate returnDate = returnedAt.toLocalDate();

        long overdueDays = Math.max(
                0,
                ChronoUnit.DAYS.between(
                        dueDate.plusDays(policy.getGracePeriodDays()),
                        returnDate
                )
        );

        if (overdueDays > 0 &&
                fineRepository.findByBorrowing_Id(borrowing.getId()).isEmpty()) {

            BigDecimal calculatedFine = policy.getFinePerDay()
                    .multiply(BigDecimal.valueOf(overdueDays));

            BigDecimal fineAmount = calculatedFine.min(
                    policy.getMaximumFine());

            Fine fine = new Fine();
            fine.setBorrowing(borrowing);
            fine.setAmount(fineAmount);
            fine.setStatus(FineStatus.UNPAID);

            fineRepository.save(fine);

            notificationService.sendNotification(
                    borrowing.getPatron().getEmail(),
                    "An overdue fine of ₹" + fineAmount
                            + " has been generated."
            );
        }



        BookCopy copy = borrowing.getBookCopy();

        if (copy.getStatus() == BookCopyStatus.BORROWED) {
            copy.setStatus(BookCopyStatus.AVAILABLE);
        }

        notificationService.sendNotification(
                email,
                "You have successfully returned "
                        + copy.getBook().getTitle()
        );

        auditService.log(
                email,
                "BOOK_RETURNED",
                "Borrowing ID: " + borrowing.getId()
        );

        return toResponse(borrowing);
    }


    private BorrowingResponseDTO toResponse(Borrowing borrowing) {

        BookCopy copy = borrowing.getBookCopy();

        return new BorrowingResponseDTO(
                borrowing.getId(),
                copy.getBook().getTitle(),
                copy.getCopyNumber(),
                borrowing.getBorrowedAt(),
                borrowing.getDueAt(),
                borrowing.getStatus()
        );
    }

    private PatronSubscription getActiveSubscription(String email) {

        return subscriptionRepository
                .findFirstByPatron_EmailAndStatusAndEndDateGreaterThanEqualOrderByEndDateDesc(
                        email,
                        MembershipStatus.ACTIVE,
                        LocalDate.now()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "An active membership subscription is required"
                ));
    }

}
