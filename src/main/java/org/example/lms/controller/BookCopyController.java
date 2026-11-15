package org.example.lms.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.example.lms.dto.BookCopyRequestDTO;
import org.example.lms.dto.BookCopyResponseDTO;
import org.example.lms.dto.BookCopyStatusUpdateDTO;
import org.example.lms.service.BookCopyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Book copies", description = "Manage physical inventory copies and copy status.")
@RestController
@RequestMapping("/books/{bookId}/copies")
public class BookCopyController {
    private final BookCopyService copyService;

    public BookCopyController(BookCopyService copyService) {
        this.copyService = copyService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BookCopyResponseDTO> addCopy(
            @PathVariable Long bookId,
            @Valid @RequestBody BookCopyRequestDTO request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                copyService.addCopy(bookId, request, authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookCopyResponseDTO>> getCopies(
            @PathVariable Long bookId) {
        return ResponseEntity.ok(copyService.getCopies(bookId));
    }

    @PatchMapping("/{copyId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BookCopyResponseDTO> updateStatus(
            @PathVariable Long bookId,
            @PathVariable Long copyId,
            @Valid @RequestBody BookCopyStatusUpdateDTO request,
            Authentication authentication) {
        // Ensure the URL's book ID matches the actual copy's parent in service layer.
        return ResponseEntity.ok(copyService.updateStatus(bookId, copyId, request, authentication.getName()));
    }

    @DeleteMapping("/{copyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Void> deleteCopy(
            @PathVariable Long bookId,
            @PathVariable Long copyId,
            Authentication authentication) {
        copyService.deleteCopy(bookId, copyId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
