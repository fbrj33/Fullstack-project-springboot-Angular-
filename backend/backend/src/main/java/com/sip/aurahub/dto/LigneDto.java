package com.sip.aurahub.dto;

import java.math.BigDecimal;

public record LigneDto(
        Long id,
        Long pieceId,
        String pieceNom,
        String taille,
        Integer quantite,
        BigDecimal prixUnitaire,
        Integer numeroExemplaire,
        Integer tirageMax) {
}
