package com.sip.aurahub.dto;

import com.sip.aurahub.entity.Niveau;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClientDto(
        Long id,
        @NotBlank String nom,
        String tel,
        @Email String email,
        String pays,
        String tailles,
        String preferences,
        Niveau niveau,
        Long conseillerId,
        String conseillerNom) {
}
