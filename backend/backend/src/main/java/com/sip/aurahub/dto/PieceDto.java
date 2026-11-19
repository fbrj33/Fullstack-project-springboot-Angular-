package com.sip.aurahub.dto;

import com.sip.aurahub.entity.Categorie;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record PieceDto(
        Long id,
        @NotBlank String reference,
        @NotBlank String nom,
        @NotNull Categorie categorie,
        String matiere,
        String couleur,
        @NotNull @DecimalMin("0.0") BigDecimal prix,
        String image,
        List<String> images,
        boolean editionLimitee,
        Integer tirageMax,
        @NotNull Long collectionId) {
}
