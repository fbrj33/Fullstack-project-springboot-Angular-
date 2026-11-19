package com.sip.aurahub.service;

import com.sip.aurahub.dto.Mappers;
import com.sip.aurahub.dto.UserDto;
import com.sip.aurahub.dto.UserRequest;
import com.sip.aurahub.entity.Role;
import com.sip.aurahub.entity.RoleName;
import com.sip.aurahub.entity.User;
import com.sip.aurahub.exception.BusinessException;
import com.sip.aurahub.exception.ResourceNotFoundException;
import com.sip.aurahub.repository.RoleRepository;
import com.sip.aurahub.repository.UserRepository;
import com.sip.aurahub.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return users.findAll().stream().map(Mappers::userDto).toList();
    }

    @Transactional(readOnly = true)
    public UserDto get(Long id) {
        return Mappers.userDto(load(id));
    }

    public User load(Long id) {
        return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }

    @Transactional
    public UserDto create(UserRequest r) {
        if (users.existsByEmail(r.email())) throw new BusinessException("Cet email est déjà utilisé");
        if (r.password() == null || r.password().length() < 6) {
            throw new BusinessException("Mot de passe obligatoire (6 caractères minimum)");
        }
        User u = new User();
        fill(u, r);
        u.setPassword(encoder.encode(r.password()));
        u.setEnabled(r.enabled() == null || r.enabled());
        return Mappers.userDto(users.saveAndFlush(u));
    }

    @Transactional
    public UserDto update(Long id, UserRequest r) {
        User u = load(id);
        if (!u.getEmail().equalsIgnoreCase(r.email()) && users.existsByEmail(r.email())) {
            throw new BusinessException("Cet email est déjà utilisé");
        }
        fill(u, r);
        if (r.password() != null && !r.password().isBlank()) {
            if (r.password().length() < 6) throw new BusinessException("Mot de passe : 6 caractères minimum");
            u.setPassword(encoder.encode(r.password()));
        }
        if (r.enabled() != null) u.setEnabled(r.enabled());
        return Mappers.userDto(users.saveAndFlush(u));
    }

    /** Validation d'un compte inscrit (appelée par l'admin). */
    @Transactional
    public UserDto activer(Long id) {
        User u = load(id);
        u.setEnabled(true);
        return Mappers.userDto(u);
    }

    @Transactional
    public void delete(Long id) {
        User u = load(id);
        if (u.getId().equals(currentUser.get().getId())) {
            throw new BusinessException("Vous ne pouvez pas supprimer votre propre compte");
        }
        users.delete(u);
        users.flush();
    }

    private void fill(User u, UserRequest r) {
        u.setEmail(r.email());
        u.setTel(r.tel());
        u.setNom(r.nom());
        u.setPrenom(r.prenom());
        Set<RoleName> wanted = (r.roles() == null || r.roles().isEmpty()) ? Set.of(RoleName.CONSEILLER) : r.roles();
        Set<Role> resolved = new HashSet<>();
        for (RoleName name : wanted) {
            resolved.add(roles.findByLibelle(name)
                    .orElseThrow(() -> new BusinessException("Rôle inconnu : " + name)));
        }
        u.setRoles(resolved);
    }
}
