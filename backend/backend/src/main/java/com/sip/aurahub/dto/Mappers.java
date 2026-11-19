package com.sip.aurahub.dto;

import com.sip.aurahub.entity.*;

import java.util.ArrayList;
import java.util.List;

/** Conversions entité -> DTO. À appeler dans une transaction (relations paresseuses). */
public final class Mappers {
    private Mappers() {}

    public static UserDto userDto(User u) {
        return new UserDto(u.getId(), u.getEmail(), u.getTel(), u.getNom(), u.getPrenom(), u.isEnabled(),
                u.getRoles().stream().map(r -> r.getLibelle().name()).sorted().toList());
    }

    public static CollectionDto collectionDto(CollectionMode c) {
        return new CollectionDto(c.getId(), c.getNom(), c.getSaison(), c.getAnnee(), c.getTheme(),
                c.getDateDefile(), c.getStatut(), c.getDateMiseEnVente());
    }

    public static PieceDto pieceDto(Piece p) {
        return new PieceDto(p.getId(), p.getReference(), p.getNom(), p.getCategorie(), p.getMatiere(),
                p.getCouleur(), p.getPrix(), p.getImage(), new ArrayList<>(p.getImages()),
                p.isEditionLimitee(), p.getTirageMax(), p.getCollection().getId());
    }

    public static PiecePublicDto piecePublic(Piece p) {
        return new PiecePublicDto(p.getId(), p.getReference(), p.getNom(), p.getCategorie(), p.getMatiere(),
                p.getCouleur(), p.getImage(), new ArrayList<>(p.getImages()), p.isEditionLimitee(), p.getTirageMax());
    }

    public static BoutiqueDto boutiqueDto(Boutique b) {
        return new BoutiqueDto(b.getId(), b.getNom(), b.getVille(), b.getPays(), b.getAdresse(), b.getType());
    }

    public static StockDto stockDto(Stock s) {
        return new StockDto(s.getId(), s.getPiece().getId(), s.getBoutique().getId(), s.getTaille(),
                s.getQuantite(), s.getPiece().getNom(), s.getBoutique().getNom());
    }

    public static ClientDto clientDto(ClientPrive c) {
        User cons = c.getConseiller();
        return new ClientDto(c.getId(), c.getNom(), c.getTel(), c.getEmail(), c.getPays(), c.getTailles(),
                c.getPreferences(), c.getNiveau(), cons.getId(), cons.getPrenom() + " " + cons.getNom());
    }

    public static LigneDto ligneDto(LigneCommande l) {
        return new LigneDto(l.getId(), l.getPiece().getId(), l.getPiece().getNom(), l.getTaille(),
                l.getQuantite(), l.getPrixUnitaire(), l.getNumeroExemplaire(), l.getPiece().getTirageMax());
    }

    public static CommandeDto commandeDto(Commande c) {
        List<LigneDto> lignes = c.getLignes().stream().map(Mappers::ligneDto).toList();
        return new CommandeDto(c.getId(), c.getDateCommande(), c.getStatut(), c.getTotal(), c.getClient().getId(),
                c.getClient().getNom(), c.getBoutique().getId(), c.getBoutique().getNom(), lignes);
    }
}
