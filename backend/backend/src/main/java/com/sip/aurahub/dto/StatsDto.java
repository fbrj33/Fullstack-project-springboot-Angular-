package com.sip.aurahub.dto;

import java.math.BigDecimal;
import java.util.List;

public record StatsDto(
        List<Montant> caParBoutique,
        List<Montant> caParCategorie,
        List<PieceVendue> piecesPlusVendues,
        List<Ecoulement> ecoulementCollections) {

    public record Montant(String libelle, BigDecimal montant) {}

    public record PieceVendue(Long pieceId, String nom, long quantiteVendue) {}

    /** Taux d'écoulement = vendus / (vendus + stock restant). */
    public record Ecoulement(Long collectionId, String nom, long vendus, long stockRestant, double taux) {}
}
