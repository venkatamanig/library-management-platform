
package org.example.lms.service;

import org.example.lms.dto.NotificationResponseDTO;
import org.example.lms.entity.AppUser;
import org.example.lms.entity.Notification;
import org.example.lms.repository.NotificationRepository;
import org.example.lms.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            EmailService emailService) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void sendNotification(String email, String message) {

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);

        emailService.sendEmail(
                email,
                "Library Management System - Notification",
                message
        );

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getMyNotifications(
            String email) {

        return notificationRepository
                .findByUser_EmailOrderByCreatedAtDesc(email)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        return notificationRepository
                .countByUser_EmailAndReadFalse(email);
    }

    @Transactional
    public void markAsRead(Long id, String email) {

        Notification notification = notificationRepository
                .findByIdAndUser_Email(id, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notification not found"
                ));

        notification.setRead(true);
    }

    private NotificationResponseDTO toResponse(
            Notification notification) {

        return new NotificationResponseDTO(
                notification.getId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
