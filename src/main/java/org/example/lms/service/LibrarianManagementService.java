
package org.example.lms.service;

import org.example.lms.dto.*;
import org.example.lms.entity.*;
import org.example.lms.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LibrarianManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public LibrarianManagementService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public LibrarianResponseDTO createLibrarian(
            LibrarianRequestDTO request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered");
        }

        AppUser librarian = new AppUser();
        librarian.setFullName(request.fullName());
        librarian.setEmail(request.email());
        librarian.setPassword(
                passwordEncoder.encode(request.password()));
        librarian.setRole(Role.LIBRARIAN);
        librarian.setStatus(AccountStatus.ACTIVE);

        AppUser saved = userRepository.save(librarian);
        auditService.logCurrentUser("LIBRARIAN_CREATED", "Created librarian " + saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LibrarianResponseDTO> getAllLibrarians() {
        return userRepository.findByRole(Role.LIBRARIAN)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LibrarianResponseDTO getLibrarianById(Long id) {
        return toResponse(getLibrarian(id));
    }

    @Transactional
    public LibrarianResponseDTO updateLibrarian(
            Long id, LibrarianUpdateDTO request) {

        AppUser librarian = getLibrarian(id);

        userRepository.findByEmail(request.email())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Email is already registered");
                });

        librarian.setFullName(request.fullName());
        librarian.setEmail(request.email());
        auditService.logCurrentUser("LIBRARIAN_UPDATED", "Updated librarian " + id);

        return toResponse(librarian);
    }

    @Transactional
    public void suspendLibrarian(Long id) {
        AppUser librarian = getLibrarian(id);

        if (librarian.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only active librarians can be suspended");
        }

        librarian.setStatus(AccountStatus.SUSPENDED);
        auditService.logCurrentUser("LIBRARIAN_SUSPENDED", "Suspended librarian " + id);
    }

    @Transactional
    public void reactivateLibrarian(Long id) {
        AppUser librarian = getLibrarian(id);

        if (librarian.getStatus() != AccountStatus.SUSPENDED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only suspended librarians can be reactivated");
        }

        librarian.setStatus(AccountStatus.ACTIVE);
        auditService.logCurrentUser("LIBRARIAN_REACTIVATED", "Reactivated librarian " + id);
    }

    private AppUser getLibrarian(Long id) {
        AppUser librarian = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Librarian not found"));

        if (librarian.getRole() != Role.LIBRARIAN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User is not a librarian");
        }

        return librarian;
    }

    private LibrarianResponseDTO toResponse(AppUser librarian) {
        return new LibrarianResponseDTO(
                librarian.getId(),
                librarian.getFullName(),
                librarian.getEmail(),
                librarian.getStatus(),
                librarian.getCreatedAt()
        );
    }
}
