package com.sip.aurahub.repository;

import com.sip.aurahub.entity.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByPieceId(Long pieceId);

    List<Stock> findByQuantiteLessThanEqual(int seuil);

    Optional<Stock> findByPieceIdAndBoutiqueIdAndTaille(Long pieceId, Long boutiqueId, String taille);

    /** Verrou pessimiste : deux confirmations simultanees ne peuvent pas depasser le stock (RG1). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.piece.id = :pid and s.boutique.id = :bid and s.taille = :taille")
    Optional<Stock> findForUpdate(@Param("pid") Long pieceId, @Param("bid") Long boutiqueId, @Param("taille") String taille);

    @Query("select sum(s.quantite) from Stock s where s.piece.collection.id = :cid")
    Long totalParCollection(@Param("cid") Long collectionId);
}
