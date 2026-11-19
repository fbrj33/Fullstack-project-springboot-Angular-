package com.sip.aurahub.repository;

import com.sip.aurahub.entity.Piece;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PieceRepository extends JpaRepository<Piece, Long> {
    List<Piece> findByCollectionId(Long collectionId);

    /** Verrou pessimiste : serialise l'attribution des numeros d'exemplaire (RG2). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Piece p where p.id = :id")
    Optional<Piece> findByIdForUpdate(@Param("id") Long id);
}
