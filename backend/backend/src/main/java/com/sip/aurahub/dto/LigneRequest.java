package com.sip.aurahub.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LigneRequest(@NotNull Long pieceId, @NotBlank String taille, @NotNull @Min(1) Integer quantite) {
}
