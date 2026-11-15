package org.example.lms.repository;

import org.example.lms.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatronSubscriptionRepository
        extends JpaRepository<PatronSubscription, Long> {

    Optional<PatronSubscription>
    findFirstByPatron_EmailAndStatusAndEndDateGreaterThanEqualOrderByEndDateDesc(
            String email,
            MembershipStatus status,
            LocalDate date
    );

    List<PatronSubscription>
    findByPatron_EmailOrderByStartDateDesc(String email);

    List<PatronSubscription>
    findByPatron_IdOrderByStartDateDesc(Long patronId);

    List<PatronSubscription> findByStatusAndEndDateBefore(
            MembershipStatus status, LocalDate date);
}
