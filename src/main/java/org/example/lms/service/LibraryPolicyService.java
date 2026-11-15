
package org.example.lms.service;

import org.example.lms.dto.LibraryPolicyRequestDTO;
import org.example.lms.dto.LibraryPolicyResponseDTO;
import org.example.lms.entity.LibraryPolicy;
import org.example.lms.repository.LibraryPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LibraryPolicyService {

    private final LibraryPolicyRepository repository;
    private final AuditService auditService;

    public LibraryPolicyService(LibraryPolicyRepository repository,
                                AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @Transactional
    public LibraryPolicyResponseDTO getPolicy() {
        return toResponse(getOrCreatePolicy());
    }

    @Transactional
    public LibraryPolicyResponseDTO updatePolicy(
            LibraryPolicyRequestDTO request) {

        if (request.maximumFine().compareTo(request.finePerDay()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Maximum fine cannot be less than the daily fine");
        }

        LibraryPolicy policy = getOrCreatePolicy();

        policy.setGracePeriodDays(request.gracePeriodDays());
        policy.setFinePerDay(request.finePerDay());
        policy.setMaximumFine(request.maximumFine());
        policy.setBlockRenewalWithUnpaidFines(
                request.blockRenewalWithUnpaidFines());

        LibraryPolicy saved = repository.save(policy);
        auditService.logCurrentUser("LIBRARY_POLICY_UPDATED", "Updated library borrowing and fine policy");
        return toResponse(saved);
    }

    private LibraryPolicy getOrCreatePolicy() {
        return repository.findById(1L).orElseGet(() -> {
            LibraryPolicy policy = new LibraryPolicy();
            return repository.save(policy);
        });
    }

    private LibraryPolicyResponseDTO toResponse(LibraryPolicy policy) {
        return new LibraryPolicyResponseDTO(
                policy.getGracePeriodDays(),
                policy.getFinePerDay(),
                policy.getMaximumFine(),
                policy.isBlockRenewalWithUnpaidFines()
        );
    }
}
