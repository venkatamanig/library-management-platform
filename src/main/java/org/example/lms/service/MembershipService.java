
package org.example.lms.service;

import org.example.lms.dto.*;
import org.example.lms.entity.*;
import org.example.lms.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class MembershipService {

    private final MembershipPlanRepository planRepository;
    private final PatronSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public MembershipService(
            MembershipPlanRepository planRepository,
            PatronSubscriptionRepository subscriptionRepository,
            UserRepository userRepository, AuditService auditService) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    @CacheEvict(cacheNames = "membership-plans", allEntries = true)
    public MembershipPlanResponseDTO createPlan(
            MembershipPlanRequestDTO request, String performedBy) {

        if (planRepository.existsByName(request.name())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Membership plan already exists");
        }

        MembershipPlan plan = new MembershipPlan();
        plan.setName(request.name());
        plan.setDurationDays(request.durationDays());
        plan.setLoanPeriodDays(request.loanPeriodDays());
        plan.setMaxBooks(request.maxBooks());
        plan.setMaxRenewals(request.maxRenewals());
        plan.setActive(true);

        MembershipPlan saved = planRepository.save(plan);
        auditService.log(performedBy, "MEMBERSHIP_PLAN_CREATED",
                "Created plan " + saved.getId() + " (" + saved.getName() + ")");
        return toPlanResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "membership-plans", key = "'active'")
    public List<MembershipPlanResponseDTO> getActivePlans() {
        return planRepository.findByActiveTrue()
                .stream()
                .map(this::toPlanResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(cacheNames = "membership-plans", allEntries = true)
    public void deactivatePlan(Long planId, String performedBy) {
        MembershipPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Membership plan not found"));

        plan.setActive(false);
        auditService.log(performedBy, "MEMBERSHIP_PLAN_DEACTIVATED",
                "Deactivated plan " + planId);
    }

    @Transactional
    public SubscriptionResponseDTO assignSubscription(
            Long patronId, Long planId, String performedBy) {

        AppUser patron = userRepository.findById(patronId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Patron not found"));

        if (patron.getRole() != Role.PATRON) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "User is not a patron");
        }

        if (patron.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Patron account is not active");
        }

        MembershipPlan plan = getActivePlan(planId);

        boolean hasActiveSubscription =
                subscriptionRepository
                        .findFirstByPatron_EmailAndStatusAndEndDateGreaterThanEqualOrderByEndDateDesc(
                                patron.getEmail(),
                                MembershipStatus.ACTIVE,
                                LocalDate.now()
                        ).isPresent();

        if (hasActiveSubscription) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Patron already has an active subscription");
        }

        PatronSubscription subscription = new PatronSubscription();
        subscription.setPatron(patron);
        subscription.setPlan(plan);
        subscription.setStartDate(LocalDate.now());
        subscription.setEndDate(
                LocalDate.now().plusDays(plan.getDurationDays()));
        subscription.setStatus(MembershipStatus.ACTIVE);

        PatronSubscription saved = subscriptionRepository.save(subscription);
        auditService.log(performedBy, "SUBSCRIPTION_ASSIGNED",
                "Assigned plan " + planId + " to patron " + patronId);
        return toSubscriptionResponse(saved);
    }

    @Transactional
    public SubscriptionResponseDTO renewSubscription(Long subscriptionId, String performedBy) {

        PatronSubscription subscription =
                subscriptionRepository.findById(subscriptionId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Subscription not found"));

        if (subscription.getStatus() == MembershipStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cancelled subscriptions cannot be renewed");
        }

        MembershipPlan plan = getActivePlan(
                subscription.getPlan().getId());

        LocalDate today = LocalDate.now();

        // Extend from the existing expiry date if still valid.
        // Otherwise, start the renewed membership today.
        LocalDate newStartDate =
                subscription.getEndDate().isBefore(today)
                        ? today
                        : subscription.getEndDate();

        subscription.setStartDate(today);
        subscription.setEndDate(
                newStartDate.plusDays(plan.getDurationDays()));
        subscription.setStatus(MembershipStatus.ACTIVE);
        auditService.log(performedBy, "SUBSCRIPTION_RENEWED",
                "Renewed subscription " + subscriptionId);

        return toSubscriptionResponse(subscription);
    }

    @Transactional
    @CacheEvict(cacheNames = "membership-plans", allEntries = true)
    public MembershipPlanResponseDTO updatePlan(Long planId,
            MembershipPlanRequestDTO request, String performedBy) {
        MembershipPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Membership plan not found"));
        if (planRepository.existsByNameAndIdNot(request.name(), planId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Another membership plan already uses that name");
        }
        plan.setName(request.name());
        plan.setDurationDays(request.durationDays());
        plan.setLoanPeriodDays(request.loanPeriodDays());
        plan.setMaxBooks(request.maxBooks());
        plan.setMaxRenewals(request.maxRenewals());
        auditService.log(performedBy, "MEMBERSHIP_PLAN_UPDATED",
                "Updated plan " + planId);
        return toPlanResponse(plan);
    }

    @Transactional
    public SubscriptionResponseDTO cancelSubscription(Long subscriptionId,
                                                       String performedBy) {
        PatronSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Subscription not found"));
        if (subscription.getStatus() == MembershipStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Subscription is already cancelled");
        }
        subscription.setStatus(MembershipStatus.CANCELLED);
        auditService.log(performedBy, "SUBSCRIPTION_CANCELLED",
                "Cancelled subscription " + subscriptionId);
        return toSubscriptionResponse(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponseDTO> getPatronSubscriptions(
            String email) {

        return subscriptionRepository
                .findByPatron_EmailOrderByStartDateDesc(email)
                .stream()
                .map(this::toSubscriptionResponse)
                .toList();
    }

    private MembershipPlan getActivePlan(Long planId) {
        MembershipPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Membership plan not found"));

        if (!plan.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Membership plan is inactive");
        }

        return plan;
    }

    private MembershipPlanResponseDTO toPlanResponse(
            MembershipPlan plan) {
        return new MembershipPlanResponseDTO(
                plan.getId(),
                plan.getName(),
                plan.getDurationDays(),
                plan.getLoanPeriodDays(),
                plan.getMaxBooks(),
                plan.getMaxRenewals(),
                plan.isActive()
        );
    }

    private SubscriptionResponseDTO toSubscriptionResponse(
            PatronSubscription subscription) {
        return new SubscriptionResponseDTO(
                subscription.getId(),
                subscription.getPatron().getEmail(),
                subscription.getPlan().getName(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getStatus()
        );
    }
}
