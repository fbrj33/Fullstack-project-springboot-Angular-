package com.sip.aurahub.service;

import com.sip.aurahub.dto.StatsDto;
import com.sip.aurahub.dto.StatsDto.Ecoulement;
import com.sip.aurahub.dto.StatsDto.Montant;
import com.sip.aurahub.dto.StatsDto.PieceVendue;
import com.sip.aurahub.entity.StatutCommande;
import com.sip.aurahub.repository.CollectionRepository;
import com.sip.aurahub.repository.CommandeRepository;
import com.sip.aurahub.repository.LigneRepository;
import com.sip.aurahub.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
    private static final List<StatutCommande> VENTES = List.of(StatutCommande.CONFIRMEE, StatutCommande.LIVREE);

    private final CommandeRepository commandes;
    private final LigneRepository lignes;
    private final StockRepository stocks;
    private final CollectionRepository collections;

    @Transactional(readOnly = true)
    public StatsDto stats() {
        List<Montant> parBoutique = commandes.caParBoutique(VENTES).stream()
                .map(r -> new Montant((String) r[0], big(r[1]))).toList();

        List<Montant> parCategorie = lignes.caParCategorie(VENTES).stream()
                .map(r -> new Montant(String.valueOf(r[0]), big(r[1]))).toList();

        List<PieceVendue> top = lignes.topPieces(VENTES, PageRequest.of(0, 10)).stream()
                .map(r -> new PieceVendue(((Number) r[0]).longValue(), (String) r[1], ((Number) r[2]).longValue()))
                .toList();

        List<Ecoulement> ecoulement = collections.findAll().stream().map(c -> {
            long vendus = nz(lignes.vendusParCollection(c.getId(), VENTES));
            long restant = nz(stocks.totalParCollection(c.getId()));
            double taux = (vendus + restant) == 0 ? 0 : (double) vendus / (vendus + restant);
            return new Ecoulement(c.getId(), c.getNom(), vendus, restant, Math.round(taux * 1000) / 10.0);
        }).toList();

        return new StatsDto(parBoutique, parCategorie, top, ecoulement);
    }

    private static BigDecimal big(Object o) {
        return o instanceof BigDecimal b ? b : new BigDecimal(String.valueOf(o));
    }

    private static long nz(Long v) {
        return v == null ? 0 : v;
    }
}
