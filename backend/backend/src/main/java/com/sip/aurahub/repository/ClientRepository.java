package com.sip.aurahub.repository;

import com.sip.aurahub.entity.ClientPrive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<ClientPrive, Long> {
    List<ClientPrive> findByConseillerId(Long conseillerId);
}
