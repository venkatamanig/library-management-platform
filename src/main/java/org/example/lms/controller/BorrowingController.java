
package org.example.lms.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.example.lms.dto.BorrowingResponseDTO;
import org.example.lms.service.BorrowingService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Borrowing", description = "Borrow, renew, return and inspect borrowing records.")
@RestController
@RequestMapping("/borrow")
public class BorrowingController {

    private final BorrowingService borrowingService;

    public BorrowingController(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    @PostMapping("/{copyId}")
    public ResponseEntity<BorrowingResponseDTO> borrowBook(
            @PathVariable Long copyId,
            Authentication authentication) {

        BorrowingResponseDTO response = borrowingService.borrowBook(
                copyId,
                authentication.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/my")
    public ResponseEntity<List<BorrowingResponseDTO>> getMyBorrowings(
            Authentication authentication) {

        return ResponseEntity.ok(
                borrowingService.getMyBorrowings(authentication.getName())
        );
    }

    @PatchMapping("/{id}/renew")
    public ResponseEntity<BorrowingResponseDTO> renewBorrowing(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                borrowingService.renewBorrowing(
                        id, authentication.getName()
                )
        );
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<BorrowingResponseDTO> returnBook(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                borrowingService.returnBook(
                        id, authentication.getName()
                )
        );
    }

}
