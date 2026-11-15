package org.example.lms.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;
import org.example.lms.dto.LoginRequestDTO;
import org.example.lms.dto.LoginResponseDTO;
import org.example.lms.dto.RegistrationRequestDTO;
import org.example.lms.security.JwtService;
import org.example.lms.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Patron registration and JWT login.")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final RegistrationService registrationService;

    public AuthController(
            RegistrationService registrationService,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.registrationService = registrationService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Register a patron", description = "Creates a patron account that awaits staff approval.")
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegistrationRequestDTO request) {

        registrationService.registerPatron(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Registration successful. Awaiting approval.");
    }

    @Operation(summary = "Log in", description = "Authenticates credentials and returns a signed JWT Bearer token.")
    @PostMapping("/login")
    public LoginResponseDTO login(
            @Valid @RequestBody LoginRequestDTO request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        String token = jwtService.generateToken(authentication);

        return new LoginResponseDTO(token, "Bearer");
    }
}
