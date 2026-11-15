package org.example.lms.repository;

import org.example.lms.entity.LibraryPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryPolicyRepository
        extends JpaRepository<LibraryPolicy, Long> {
}
