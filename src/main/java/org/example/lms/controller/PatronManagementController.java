
package org.example.lms.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.example.lms.dto.PatronDetailsResponseDTO;
import org.example.lms.dto.PatronResponseDTO;
import org.example.lms.dto.PatronUpdateDTO;
import org.example.lms.service.PatronManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Patrons", description = "Search, approve, suspend and reactivate patrons.")
@RestController
@RequestMapping("/patron-management/patrons")
public class PatronManagementController {

    private final PatronManagementService patronManagementService;

    public PatronManagementController(
            PatronManagementService patronManagementService) {
        this.patronManagementService = patronManagementService;
    }

    // Get all patrons
    @GetMapping
    public ResponseEntity<List<PatronResponseDTO>> getAllPatrons() {
        return ResponseEntity.ok(
                patronManagementService.getAllPatrons());
    }

    // Search patrons by name or email
    @GetMapping("/search")
    public ResponseEntity<List<PatronResponseDTO>> searchPatrons(
            @RequestParam String keyword) {

        return ResponseEntity.ok(
                patronManagementService.searchPatrons(keyword));
    }

    // Get patron by ID
    @GetMapping("/{id}")
    public ResponseEntity<PatronResponseDTO> getPatronById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                patronManagementService.getPatronById(id));
    }

    // Get complete patron details
    @GetMapping("/{id}/details")
    public ResponseEntity<PatronDetailsResponseDTO> getPatronDetails(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                patronManagementService.getPatronDetails(id));
    }

    // Update patron profile
    @PutMapping("/{id}")
    public ResponseEntity<PatronResponseDTO> updatePatron(
            @PathVariable Long id,
            @Valid @RequestBody PatronUpdateDTO request) {

        return ResponseEntity.ok(
                patronManagementService.updatePatron(id, request));
    }

    // Approve pending patron
    @PatchMapping("/{id}/approve")
    public ResponseEntity<String> approvePatron(
            @PathVariable Long id) {

        patronManagementService.approvePatron(id);
        return ResponseEntity.ok(
                "Patron approved successfully.");
    }

    // Suspend patron
    @PatchMapping("/{id}/suspend")
    public ResponseEntity<String> suspendPatron(
            @PathVariable Long id) {

        patronManagementService.suspendPatron(id);
        return ResponseEntity.ok(
                "Patron suspended successfully.");
    }

    // Reactivate suspended patron
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<String> reactivatePatron(
            @PathVariable Long id) {

        patronManagementService.reactivatePatron(id);
        return ResponseEntity.ok(
                "Patron reactivated successfully.");
    }
}
