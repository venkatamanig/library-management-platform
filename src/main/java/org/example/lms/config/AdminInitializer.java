package org.example.lms.config;

import org.example.lms.entity.AccountStatus;
import org.example.lms.entity.AppUser;
import org.example.lms.entity.Role;
import org.example.lms.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public AdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Environment environment) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Spring normally exposes OS environment variables through Environment.
        // System.getenv is an explicit fallback to make the expected source clear
        // and to diagnose IDE/Gradle run-configuration mistakes.
        String email = firstNonBlank(
                environment.getProperty("LMS_ADMIN_EMAIL"),
                System.getenv("LMS_ADMIN_EMAIL"));
        String password = firstNonBlank(
                environment.getProperty("LMS_ADMIN_PASSWORD"),
                System.getenv("LMS_ADMIN_PASSWORD"));
        String name = firstNonBlank(
                environment.getProperty("LMS_ADMIN_NAME"),
                System.getenv("LMS_ADMIN_NAME"),
                "Library Admin");

        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            System.err.println("Initial admin not created. Process environment visibility: "
                    + "LMS_ADMIN_EMAIL=" + StringUtils.hasText(System.getenv("LMS_ADMIN_EMAIL"))
                    + ", LMS_ADMIN_PASSWORD=" + StringUtils.hasText(System.getenv("LMS_ADMIN_PASSWORD"))
                    + ". Configure variables on the actual Application/bootRun process, not only the Gradle daemon.");
            return;
        }

        if (userRepository.existsByEmail(email.trim())) {
            System.out.println("Initial admin already exists; no changes made.");
            return;
        }

        AppUser admin = new AppUser();
        admin.setFullName(name.trim());
        admin.setEmail(email.trim());
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        admin.setStatus(AccountStatus.ACTIVE);

        try {
            userRepository.saveAndFlush(admin);
            System.out.println("Initial admin account created from external configuration.");
        } catch (DataIntegrityViolationException concurrentStartup) {
            // Multiple LMS replicas may run this initializer simultaneously.
            if (userRepository.existsByEmail(email.trim())) {
                System.out.println("Initial admin was created by another LMS instance.");
                return;
            }
            throw concurrentStartup;
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }
}
