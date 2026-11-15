
package org.example.lms.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.example.lms.dto.NotificationResponseDTO;
import org.example.lms.service.NotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Notifications", description = "Read and manage patron notifications.")
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<NotificationResponseDTO>>
    getMyNotifications(Authentication authentication) {

        return ResponseEntity.ok(
                notificationService.getMyNotifications(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            Authentication authentication) {

        return ResponseEntity.ok(
                Map.of(
                        "unreadCount",
                        notificationService.getUnreadCount(
                                authentication.getName()
                        )
                )
        );
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        notificationService.markAsRead(
                id,
                authentication.getName()
        );

        return ResponseEntity.ok(
                "Notification marked as read."
        );
    }
}
