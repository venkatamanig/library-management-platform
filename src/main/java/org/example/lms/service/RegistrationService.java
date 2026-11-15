package org.example.lms.service;

import org.example.lms.dto.RegistrationRequestDTO;
import org.example.lms.entity.AccountStatus;
import org.example.lms.entity.AppUser;
import org.example.lms.entity.Role;
import org.example.lms.exception.EmailAlreadyRegisteredException;
import org.example.lms.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerPatron(RegistrationRequestDTO request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyRegisteredException(request.getEmail());
        }

        AppUser user = new AppUser();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(Role.PATRON);
        user.setStatus(AccountStatus.PENDING);

        userRepository.save(user);
    }
}
