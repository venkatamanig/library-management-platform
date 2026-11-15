
package org.example.lms.service;

import org.example.lms.dto.*;
import org.example.lms.entity.*;
import org.example.lms.repository.BorrowingRepository;
import org.example.lms.repository.FineRepository;
import org.example.lms.repository.PatronSubscriptionRepository;
import org.example.lms.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PatronManagementService {

    private final UserRepository userRepository;
    private final BorrowingRepository borrowingRepository;
    private final FineRepository fineRepository;
    private final PatronSubscriptionRepository subscriptionRepository;
    private final AuditService auditService;

    public PatronManagementService(UserRepository userRepository,
                                   BorrowingRepository borrowingRepository,
                                   FineRepository fineRepository,
                                   PatronSubscriptionRepository subscriptionRepository,
                                   AuditService auditService) {
        this.userRepository = userRepository;
        this.borrowingRepository = borrowingRepository;
        this.fineRepository = fineRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void approvePatron(Long userId) {

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + userId));

        if (user.getRole() != Role.PATRON) {
            throw new IllegalArgumentException(
                    "Only patron accounts can be approved"
            );
        }

        if (user.getStatus() != AccountStatus.PENDING) {
            throw new IllegalStateException(
                    "Patron account is not pending approval"
            );
        }

        user.setStatus(AccountStatus.ACTIVE);
        auditService.logCurrentUser("PATRON_APPROVED", "Approved patron " + userId);
    }

    @Transactional
    public void suspendPatron(Long userId) {

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + userId));

        if (user.getRole() != Role.PATRON) {
            throw new IllegalArgumentException(
                    "Only patron accounts can be suspended"
            );
        }

        user.setStatus(AccountStatus.SUSPENDED);
        auditService.logCurrentUser("PATRON_SUSPENDED", "Suspended patron " + userId);
    }

    @Transactional(readOnly = true)
    public List<PatronResponseDTO> getAllPatrons() {

        return userRepository.findByRole(Role.PATRON)
                .stream()
                .map(this::toPatronResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PatronResponseDTO> searchPatrons(String keyword) {

        List<AppUser> patrons = new ArrayList<>();

        patrons.addAll(
                userRepository
                        .findByRoleAndFullNameContainingIgnoreCase(
                                Role.PATRON, keyword));

        userRepository
                .findByRoleAndEmailContainingIgnoreCase(
                        Role.PATRON, keyword)
                .forEach(user -> {
                    if (patrons.stream().noneMatch(
                            existing -> existing.getId().equals(user.getId()))) {
                        patrons.add(user);
                    }
                });

        return patrons.stream()
                .map(this::toPatronResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PatronResponseDTO getPatronById(Long id) {

        AppUser patron = getPatron(id);
        return toPatronResponse(patron);
    }

    @Transactional
    public PatronResponseDTO updatePatron(
            Long id, PatronUpdateDTO request) {

        AppUser patron = getPatron(id);

        userRepository.findByEmail(request.email())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Email is already registered");
                });

        patron.setFullName(request.fullName());
        patron.setEmail(request.email());
        auditService.logCurrentUser("PATRON_UPDATED", "Updated patron " + id);

        return toPatronResponse(patron);
    }

    @Transactional
    public void reactivatePatron(Long id) {

        AppUser patron = getPatron(id);

        if (patron.getStatus() != AccountStatus.SUSPENDED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only suspended patrons can be reactivated");
        }

        patron.setStatus(AccountStatus.ACTIVE);
        auditService.logCurrentUser("PATRON_REACTIVATED", "Reactivated patron " + id);
    }

    private AppUser getPatron(Long id) {

        AppUser patron = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Patron not found"));

        if (patron.getRole() != Role.PATRON) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "User is not a patron");
        }

        return patron;
    }

    private PatronResponseDTO toPatronResponse(AppUser patron) {

        return new PatronResponseDTO(
                patron.getId(),
                patron.getFullName(),
                patron.getEmail(),
                patron.getStatus(),
                patron.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public PatronDetailsResponseDTO getPatronDetails(Long patronId) {

        AppUser patron = getPatron(patronId);

        List<BorrowingResponseDTO> borrowings =
                borrowingRepository
                        .findByPatron_IdOrderByBorrowedAtDesc(patronId)
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

        List<Fine> patronFines =
                fineRepository
                        .findByBorrowing_Patron_IdOrderByCreatedAtDesc(patronId);

        List<FineResponseDTO> fines = patronFines.stream()
                .map(f -> new FineResponseDTO(
                        f.getId(),
                        f.getBorrowing().getId(),
                        f.getBorrowing().getBookCopy().getBook().getTitle(),
                        f.getAmount(),
                        f.getStatus(),
                        f.getCreatedAt(),
                        f.getPaidAt()
                ))
                .toList();

        List<SubscriptionResponseDTO> subscriptions =
                subscriptionRepository
                        .findByPatron_IdOrderByStartDateDesc(patronId)
                        .stream()
                        .map(s -> new SubscriptionResponseDTO(
                                s.getId(),
                                patron.getEmail(),
                                s.getPlan().getName(),
                                s.getStartDate(),
                                s.getEndDate(),
                                s.getStatus()
                        ))
                        .toList();

        BigDecimal outstandingBalance = patronFines.stream()
                .filter(f -> f.getStatus() == FineStatus.UNPAID)
                .map(Fine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PatronDetailsResponseDTO(
                toPatronResponse(patron),
                borrowings,
                fines,
                subscriptions,
                outstandingBalance
        );
    }
}
