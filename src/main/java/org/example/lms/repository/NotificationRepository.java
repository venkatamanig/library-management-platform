
package org.example.lms.repository;

import org.example.lms.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_EmailOrderByCreatedAtDesc(
            String email
    );

    Optional<Notification> findByIdAndUser_Email(
            Long id,
            String email
    );

    long countByUser_EmailAndReadFalse(String email);
}
