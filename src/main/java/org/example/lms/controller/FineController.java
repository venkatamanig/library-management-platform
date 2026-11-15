
package org.example.lms.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.example.lms.dto.FineResponseDTO;
import org.example.lms.service.FineService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Fines", description = "View outstanding fines and record fine payments.")
@RestController
@RequestMapping("/fines")
public class FineController {

    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATRON')")
    public ResponseEntity<List<FineResponseDTO>> getMyFines(
            Authentication authentication) {

        return ResponseEntity.ok(
                fineService.getMyFines(authentication.getName())
        );
    }

    @GetMapping("/unpaid")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<FineResponseDTO>> getUnpaidFines() {

        return ResponseEntity.ok(fineService.getUnpaidFines());
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<FineResponseDTO> payFine(
            @PathVariable Long id, Authentication authentication) {

        return ResponseEntity.ok(fineService.payFine(id, authentication.getName()));
    }
}
