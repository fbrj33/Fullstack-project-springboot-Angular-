package com.sip.aurahub.dto;

import com.sip.aurahub.entity.StatutCommande;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CommandeDto(
        Long id,
        LocalDateTime date,
        StatutCommande statut,
        BigDecimal total,
        Long clientId,
        String clientNom,
        Long boutiqueId,
        String boutiqueNom,
        List<LigneDto> lignes) {
}
