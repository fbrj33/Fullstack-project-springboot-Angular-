package com.sip.aurahub.repository;

import com.sip.aurahub.entity.LigneCommande;
import com.sip.aurahub.entity.StatutCommande;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface LigneRepository extends JpaRepository<LigneCommande, Long> {

    /** Numeros d'exemplaire deja attribues pour une piece (RG2). */
    @Query("select l.numeroExemplaire from LigneCommande l where l.piece.id = :pid and l.numeroExemplaire is not null")
    List<Integer> numerosUtilises(@Param("pid") Long pieceId);

    @Query("select l.piece.categorie, sum(l.prixUnitaire * l.quantite) from LigneCommande l " +
           "where l.commande.statut in :st group by l.piece.categorie")
    List<Object[]> caParCategorie(@Param("st") Collection<StatutCommande> statuts);

    @Query("select l.piece.id, l.piece.nom, sum(l.quantite) from LigneCommande l " +
           "where l.commande.statut in :st group by l.piece.id, l.piece.nom order by sum(l.quantite) desc")
    List<Object[]> topPieces(@Param("st") Collection<StatutCommande> statuts, Pageable pageable);

    @Query("select sum(l.quantite) from LigneCommande l " +
           "where l.piece.collection.id = :cid and l.commande.statut in :st")
    Long vendusParCollection(@Param("cid") Long collectionId, @Param("st") Collection<StatutCommande> statuts);
}
