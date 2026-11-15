
package org.example.lms.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.example.lms.dto.*;
import org.example.lms.service.MembershipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Memberships", description = "Manage membership plans and patron subscriptions.")
@RestController
@RequestMapping("/memberships")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    // Admin and Librarian: create a plan
    @PostMapping("/plans")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<MembershipPlanResponseDTO> createPlan(
            @Valid @RequestBody MembershipPlanRequestDTO request,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(membershipService.createPlan(request, authentication.getName()));
    }

    // Authenticated users: view available plans
    @GetMapping("/plans")
    public ResponseEntity<List<MembershipPlanResponseDTO>> getPlans() {
        return ResponseEntity.ok(membershipService.getActivePlans());
    }

    // Admin and Librarian: deactivate a plan
    @PatchMapping("/plans/{planId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<String> deactivatePlan(
            @PathVariable Long planId, Authentication authentication) {

        membershipService.deactivatePlan(planId, authentication.getName());
        return ResponseEntity.ok("Membership plan deactivated.");
    }

    // Admin and Librarian: assign a plan to a patron
    @PostMapping("/patrons/{patronId}/assign/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<SubscriptionResponseDTO> assignSubscription(
            @PathVariable Long patronId,
            @PathVariable Long planId, Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(membershipService.assignSubscription(
                        patronId, planId, authentication.getName()));
    }

    // Admin and Librarian: renew a subscription
    @PatchMapping("/{subscriptionId}/renew")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<SubscriptionResponseDTO> renewSubscription(
            @PathVariable Long subscriptionId, Authentication authentication) {

        return ResponseEntity.ok(
                membershipService.renewSubscription(subscriptionId, authentication.getName()));
    }

    @PutMapping("/plans/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<MembershipPlanResponseDTO> updatePlan(
            @PathVariable Long planId,
            @Valid @RequestBody MembershipPlanRequestDTO request,
            Authentication authentication) {
        return ResponseEntity.ok(membershipService.updatePlan(
                planId, request, authentication.getName()));
    }

    @PatchMapping("/{subscriptionId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<SubscriptionResponseDTO> cancelSubscription(
            @PathVariable Long subscriptionId, Authentication authentication) {
        return ResponseEntity.ok(membershipService.cancelSubscription(
                subscriptionId, authentication.getName()));
    }

    // Patron: view their own subscription history
    @GetMapping("/my")
    @PreAuthorize("hasRole('PATRON')")
    public ResponseEntity<List<SubscriptionResponseDTO>> getMySubscriptions(
            Authentication authentication) {

        return ResponseEntity.ok(
                membershipService.getPatronSubscriptions(
                        authentication.getName()));
    }
}
