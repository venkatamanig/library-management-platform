
package org.example.lms.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.example.lms.dto.*;
import org.example.lms.service.LibrarianManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Librarians", description = "Create and manage librarian accounts.")
@RestController
@RequestMapping("/admin/librarians")
@PreAuthorize("hasRole('ADMIN')")
public class LibrarianManagementController {

    private final LibrarianManagementService service;

    public LibrarianManagementController(
            LibrarianManagementService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<LibrarianResponseDTO> create(
            @Valid @RequestBody LibrarianRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createLibrarian(request));
    }

    @GetMapping
    public ResponseEntity<List<LibrarianResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAllLibrarians());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LibrarianResponseDTO> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.getLibrarianById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LibrarianResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody LibrarianUpdateDTO request) {
        return ResponseEntity.ok(
                service.updateLibrarian(id, request));
    }

    @PatchMapping("/{id}/suspend")
    public ResponseEntity<String> suspend(@PathVariable Long id) {
        service.suspendLibrarian(id);
        return ResponseEntity.ok("Librarian suspended successfully.");
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<String> reactivate(@PathVariable Long id) {
        service.reactivateLibrarian(id);
        return ResponseEntity.ok("Librarian reactivated successfully.");
    }
}
