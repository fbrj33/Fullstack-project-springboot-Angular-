package com.sip.aurahub.service;

import com.sip.aurahub.dto.AuthResponse;
import com.sip.aurahub.dto.LoginRequest;
import com.sip.aurahub.dto.RegisterRequest;
import com.sip.aurahub.entity.Role;
import com.sip.aurahub.entity.RoleName;
import com.sip.aurahub.entity.User;
import com.sip.aurahub.exception.BusinessException;
import com.sip.aurahub.repository.RoleRepository;
import com.sip.aurahub.repository.UserRepository;
import com.sip.aurahub.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final MailService mail;

    /** Inscription : compte désactivé + role CONSEILLER, l'admin est prévenu par email. */
    @Transactional
    public void register(RegisterRequest r) {
        if (users.existsByEmail(r.email())) {
            throw new BusinessException("Cet email est déjà utilisé");
        }
        Role conseiller = roles.findByLibelle(RoleName.CONSEILLER)
                .orElseThrow(() -> new BusinessException("Rôle CONSEILLER manquant"));
        User u = new User();
        u.setEmail(r.email());
        u.setPassword(encoder.encode(r.password()));
        u.setTel(r.tel());
        u.setNom(r.nom());
        u.setPrenom(r.prenom());
        u.setEnabled(false);
        u.getRoles().add(conseiller);
        users.save(u);
        mail.notifierInscription(u);
    }

    /** Lève DisabledException (-> 403) si le compte n'est pas encore validé, BadCredentials (-> 401) sinon. */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest r) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(r.email(), r.password()));
        User u = users.findByEmail(r.email()).orElseThrow();
        return new AuthResponse(jwt.generate(u), u.getId(), u.getEmail(), u.getNom(), u.getPrenom(),
                u.getRoles().stream().map(x -> x.getLibelle().name()).sorted().toList());
    }
}
