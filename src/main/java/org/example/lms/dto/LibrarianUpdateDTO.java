package org.example.lms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LibrarianUpdateDTO(

        @NotBlank
        @Size(max = 100)
        String fullName,

        @NotBlank
        @Email
        String email
) {}
