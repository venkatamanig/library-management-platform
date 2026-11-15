
package org.example.lms.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.example.lms.dto.DashboardResponseDTO;
import org.example.lms.dto.BorrowingResponseDTO;
import org.example.lms.service.ReportService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Reports", description = "Library dashboard, borrowing, overdue and fine reports.")
@RestController
@RequestMapping("/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponseDTO> getDashboard() {
        return ResponseEntity.ok(reportService.getDashboard());
    }

    @GetMapping("/borrowings")
    public ResponseEntity<List<BorrowingResponseDTO>> getAllBorrowings() {
        return ResponseEntity.ok(reportService.getAllBorrowings());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowingResponseDTO>> getOverdueBorrowings() {
        return ResponseEntity.ok(reportService.getOverdueBorrowings());
    }

    @GetMapping("/fines")
    public ResponseEntity<java.util.Map<String, Object>> getFineReport() {
        return ResponseEntity.ok(reportService.getFineReport());
    }
}
