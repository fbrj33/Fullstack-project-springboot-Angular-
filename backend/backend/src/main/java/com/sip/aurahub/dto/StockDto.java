package com.sip.aurahub.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StockDto(
        Long id,
        @NotNull Long pieceId,
        @NotNull Long boutiqueId,
        @NotBlank String taille,
        @NotNull @Min(0) Integer quantite,
        String pieceNom,
        String boutiqueNom) {
}
