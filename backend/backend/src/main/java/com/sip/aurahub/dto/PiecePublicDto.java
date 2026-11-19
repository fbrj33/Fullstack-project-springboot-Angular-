package com.sip.aurahub.dto;

import com.sip.aurahub.entity.Categorie;

import java.util.List;

/** Vue publique d'une piece : volontairement SANS prix. */
public record PiecePublicDto(
        Long id,
        String reference,
        String nom,
        Categorie categorie,
        String matiere,
        String couleur,
        String image,
        List<String> images,
        boolean editionLimitee,
        Integer tirageMax) {
}
