package com.sip.aurahub.config;

import com.sip.aurahub.entity.Role;
import com.sip.aurahub.entity.RoleName;
import com.sip.aurahub.entity.User;
import com.sip.aurahub.repository.RoleRepository;
import com.sip.aurahub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Au premier démarrage : crée les rôles et un compte ADMIN (identifiants dans application.properties). */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(RoleRepository roles, UserRepository users, PasswordEncoder encoder,
                               @Value("${app.admin.email}") String adminEmail,
                               @Value("${app.admin.password}") String adminPassword) {
        return args -> {
            for (RoleName name : RoleName.values()) {
                if (roles.findByLibelle(name).isEmpty()) roles.save(new Role(name));
            }
            if (!users.existsByEmail(adminEmail)) {
                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setPassword(encoder.encode(adminPassword));
                admin.setNom("Aura");
                admin.setPrenom("Admin");
                admin.setEnabled(true);
                admin.getRoles().add(roles.findByLibelle(RoleName.ADMIN).orElseThrow());
                users.save(admin);
            }
        };
    }
}
