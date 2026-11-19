package com.sip.aurahub.service;

import com.sip.aurahub.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender sender;
    private final String adminEmail;
    private final String from;
    private final String baseUrl;

    public MailService(JavaMailSender sender,
                       @Value("${app.admin.email}") String adminEmail,
                       @Value("${app.mail.from}") String from,
                       @Value("${app.base-url}") String baseUrl) {
        this.sender = sender;
        this.adminEmail = adminEmail;
        this.from = from;
        this.baseUrl = baseUrl;
    }

    /** Prévient l'admin qu'un compte attend sa validation. Un échec d'envoi ne bloque pas l'inscription. */
    public void notifierInscription(User u) {
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setFrom(from);
            m.setTo(adminEmail);
            m.setSubject("Aura Hub - nouvelle inscription à valider");
            m.setText("Un nouveau compte attend votre validation :\n\n"
                    + "Nom : " + u.getPrenom() + " " + u.getNom() + "\n"
                    + "Email : " + u.getEmail() + "\n"
                    + "Téléphone : " + (u.getTel() == null ? "-" : u.getTel()) + "\n\n"
                    + "Activez-le depuis l'espace Utilisateurs (" + baseUrl + ") "
                    + "ou via PUT /api/utilisateurs/" + u.getId() + "/activer\n");
            sender.send(m);
        } catch (Exception e) {
            log.warn("Email d'inscription non envoyé : {}", e.getMessage());
        }
    }
}
