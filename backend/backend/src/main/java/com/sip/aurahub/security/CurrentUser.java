package com.sip.aurahub.security;

import com.sip.aurahub.entity.RoleName;
import com.sip.aurahub.entity.User;
import com.sip.aurahub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Donne accès à l'utilisateur connecté (déduit du JWT). */
@Component
@RequiredArgsConstructor
public class CurrentUser {
    private final UserRepository users;

    public User get() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || "anonymousUser".equals(a.getPrincipal())) {
            throw new AccessDeniedException("Non authentifié");
        }
        return users.findByEmail(a.getName()).orElseThrow(() -> new AccessDeniedException("Utilisateur inconnu"));
    }

    public boolean isAdmin() {
        return get().hasRole(RoleName.ADMIN);
    }
}
