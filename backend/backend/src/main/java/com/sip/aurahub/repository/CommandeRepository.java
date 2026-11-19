package com.sip.aurahub.repository;

import com.sip.aurahub.entity.Commande;
import com.sip.aurahub.entity.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Long> {
    List<Commande> findAllByOrderByDateCommandeDesc();

    List<Commande> findByClientIdOrderByDateCommandeDesc(Long clientId);

    List<Commande> findByClientConseillerIdOrderByDateCommandeDesc(Long conseillerId);

    /** RG7 : cumul d'achats d'un client depuis une date. */
    @Query("select sum(c.total) from Commande c where c.client.id = :cid and c.statut in :st and c.dateCommande >= :since")
    BigDecimal cumulDepuis(@Param("cid") Long clientId,
                           @Param("st") Collection<StatutCommande> statuts,
                           @Param("since") LocalDateTime since);

    @Query("select c.boutique.nom, sum(c.total) from Commande c where c.statut in :st " +
           "group by c.boutique.nom order by sum(c.total) desc")
    List<Object[]> caParBoutique(@Param("st") Collection<StatutCommande> statuts);
}
