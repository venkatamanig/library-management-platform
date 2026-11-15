package org.example.lms.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.example.lms.dto.LibraryPolicyRequestDTO;
import org.example.lms.dto.LibraryPolicyResponseDTO;
import org.example.lms.service.LibraryPolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Library policies", description = "Read and update library circulation and fine policies.")
@RestController
@RequestMapping("/admin/policies")
public class LibraryPolicyController {
    private final LibraryPolicyService policyService;

    public LibraryPolicyController(LibraryPolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<LibraryPolicyResponseDTO> getPolicy() {
        return ResponseEntity.ok(policyService.getPolicy());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LibraryPolicyResponseDTO> updatePolicy(
            @Valid @RequestBody LibraryPolicyRequestDTO request) {
        return ResponseEntity.ok(policyService.updatePolicy(request));
    }
}
