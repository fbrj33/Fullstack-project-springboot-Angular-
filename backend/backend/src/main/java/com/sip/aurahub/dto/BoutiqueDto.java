package com.sip.aurahub.dto;

import com.sip.aurahub.entity.TypeBoutique;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BoutiqueDto(
        Long id,
        @NotBlank String nom,
        @NotBlank String ville,
        @NotBlank String pays,
        String adresse,
        @NotNull TypeBoutique type) {
}
