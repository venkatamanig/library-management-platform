package org.example.lms.repository;

import org.example.lms.entity.AccountStatus;
import org.example.lms.entity.AppUser;
import org.example.lms.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByEmail(String email);

    Optional<AppUser> findByEmail(String email);

    long countByRole(Role role);

    long countByRoleAndStatus(Role role, AccountStatus status);

    List<AppUser> findByRole(Role role);

    List<AppUser> findByRoleAndFullNameContainingIgnoreCase(
            Role role, String fullName);

    List<AppUser> findByRoleAndEmailContainingIgnoreCase(
            Role role, String email);
}
