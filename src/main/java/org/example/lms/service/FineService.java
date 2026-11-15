
package org.example.lms.service;

import org.example.lms.dto.FineResponseDTO;
import org.example.lms.entity.Fine;
import org.example.lms.entity.FineStatus;
import org.example.lms.repository.FineRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FineService {

    private final FineRepository fineRepository;
    private final AuditService auditService;

    public FineService(FineRepository fineRepository, AuditService auditService) {
        this.fineRepository = fineRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<FineResponseDTO> getMyFines(String email) {
        return fineRepository
                .findByBorrowing_Patron_EmailOrderByCreatedAtDesc(email)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FineResponseDTO> getUnpaidFines() {
        return fineRepository.findByStatus(FineStatus.UNPAID)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FineResponseDTO payFine(Long id, String performedBy) {

        Fine fine = fineRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Fine not found"
                ));

        if (fine.getStatus() == FineStatus.PAID) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Fine has already been paid"
            );
        }

        fine.setStatus(FineStatus.PAID);
        fine.setPaidAt(LocalDateTime.now());
        auditService.log(performedBy, "FINE_PAID",
                "Fine " + id + " paid; amount=" + fine.getAmount());

        return toResponse(fine);
    }

    private FineResponseDTO toResponse(Fine fine) {

        return new FineResponseDTO(
                fine.getId(),
                fine.getBorrowing().getId(),
                fine.getBorrowing().getBookCopy()
                        .getBook().getTitle(),
                fine.getAmount(),
                fine.getStatus(),
                fine.getCreatedAt(),
                fine.getPaidAt()
        );
    }
}
