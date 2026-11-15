package org.example.lms.dto;

public record LoginResponseDTO(
        String accessToken,
        String tokenType
) {}
