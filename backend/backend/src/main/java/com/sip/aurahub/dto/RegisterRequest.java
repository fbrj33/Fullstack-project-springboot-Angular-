package com.sip.aurahub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, message = "6 caractères minimum") String password,
        String tel,
        @NotBlank String nom,
        @NotBlank String prenom) {
}
