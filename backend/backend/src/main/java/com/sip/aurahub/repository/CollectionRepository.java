package com.sip.aurahub.repository;

import com.sip.aurahub.entity.CollectionMode;
import com.sip.aurahub.entity.StatutCollection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionRepository extends JpaRepository<CollectionMode, Long> {
    List<CollectionMode> findByStatut(StatutCollection statut);
}
