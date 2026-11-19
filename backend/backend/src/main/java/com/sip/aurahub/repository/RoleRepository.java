package com.sip.aurahub.repository;

import com.sip.aurahub.entity.Role;
import com.sip.aurahub.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByLibelle(RoleName libelle);
}
