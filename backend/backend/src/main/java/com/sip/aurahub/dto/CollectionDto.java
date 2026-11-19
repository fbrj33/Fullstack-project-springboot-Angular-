package com.sip.aurahub.dto;

import com.sip.aurahub.entity.Saison;
import com.sip.aurahub.entity.StatutCollection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CollectionDto(
        Long id,
        @NotBlank String nom,
        @NotNull Saison saison,
        @NotNull Integer annee,
        String theme,
        LocalDate dateDefile,
        StatutCollection statut,
        LocalDateTime dateMiseEnVente) {
}
