package com.sip.aurahub.service;

import com.sip.aurahub.dto.ClientDto;
import com.sip.aurahub.dto.HistoriqueDto;
import com.sip.aurahub.dto.Mappers;
import com.sip.aurahub.entity.ClientPrive;
import com.sip.aurahub.entity.Niveau;
import com.sip.aurahub.entity.StatutCommande;
import com.sip.aurahub.entity.User;
import com.sip.aurahub.exception.ResourceNotFoundException;
import com.sip.aurahub.repository.ClientRepository;
import com.sip.aurahub.repository.CommandeRepository;
import com.sip.aurahub.repository.UserRepository;
import com.sip.aurahub.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository repo;
    private final CommandeRepository commandes;
    private final UserRepository users;
    private final CurrentUser currentUser;

    @Value("${app.niveau.prestige:5000}")
    private BigDecimal seuilPrestige;
    @Value("${app.niveau.elite:20000}")
    private BigDecimal seuilElite;

    /** RG5 : un CONSEILLER ne voit que ses propres clients, l'ADMIN voit tout. */
    @Transactional(readOnly = true)
    public List<ClientDto> findAll() {
        List<ClientPrive> list = currentUser.isAdmin()
                ? repo.findAll()
                : repo.findByConseillerId(currentUser.get().getId());
        return list.stream().map(Mappers::clientDto).toList();
    }

    @Transactional(readOnly = true)
    public ClientDto get(Long id) {
        return Mappers.clientDto(loadAuthorized(id));
    }

    /** Charge un client en vérifiant le droit d'accès (RG5). */
    public ClientPrive loadAuthorized(Long id) {
        ClientPrive c = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : " + id));
        checkAccess(c);
        return c;
    }

    public void checkAccess(ClientPrive c) {
        if (!currentUser.isAdmin() && !c.getConseiller().getId().equals(currentUser.get().getId())) {
            throw new AccessDeniedException("Ce client n'est pas rattaché à votre portefeuille");
        }
    }

    @Transactional
    public ClientDto create(ClientDto d) {
        ClientPrive c = new ClientPrive();
        apply(c, d);
        c.setConseiller(resolveConseiller(d));
        c.setNiveau(d.niveau() == null || !currentUser.isAdmin() ? Niveau.PRIVILEGE : d.niveau());
        return Mappers.clientDto(repo.saveAndFlush(c));
    }

    @Transactional
    public ClientDto update(Long id, ClientDto d) {
        ClientPrive c = loadAuthorized(id);
        apply(c, d);
        if (currentUser.isAdmin()) {                       // seul l'admin réaffecte ou fixe le niveau à la main
            if (d.conseillerId() != null) c.setConseiller(resolveConseiller(d));
            if (d.niveau() != null) c.setNiveau(d.niveau());
        }
        return Mappers.clientDto(repo.saveAndFlush(c));
    }

    @Transactional
    public void delete(Long id) {
        repo.delete(loadAuthorized(id));
        repo.flush();
    }

    /** Fiche client : profil + achats + total. */
    @Transactional(readOnly = true)
    public HistoriqueDto historique(Long id) {
        ClientPrive c = loadAuthorized(id);
        var cmds = commandes.findByClientIdOrderByDateCommandeDesc(id).stream().map(Mappers::commandeDto).toList();
        BigDecimal total = cmds.stream()
                .filter(x -> x.statut() == StatutCommande.CONFIRMEE || x.statut() == StatutCommande.LIVREE)
                .map(x -> x.total())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new HistoriqueDto(Mappers.clientDto(c), cmds, total);
    }

    /** RG7 : niveau selon le cumul d'achats (confirmés/livrés) des 12 derniers mois. */
    public void recalculerNiveau(ClientPrive c) {
        BigDecimal cumul = commandes.cumulDepuis(c.getId(),
                List.of(StatutCommande.CONFIRMEE, StatutCommande.LIVREE), LocalDateTime.now().minusMonths(12));
        if (cumul == null) cumul = BigDecimal.ZERO;
        Niveau n = cumul.compareTo(seuilElite) >= 0 ? Niveau.ELITE
                : cumul.compareTo(seuilPrestige) >= 0 ? Niveau.PRESTIGE
                : Niveau.PRIVILEGE;
        c.setNiveau(n);
    }

    /** Recalcul nocturne : les achats de plus de 12 mois sortent de la fenêtre glissante. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void recalculerTous() {
        repo.findAll().forEach(this::recalculerNiveau);
    }

    private User resolveConseiller(ClientDto d) {
        if (!currentUser.isAdmin() || d.conseillerId() == null) return currentUser.get();
        return users.findById(d.conseillerId())
                .orElseThrow(() -> new ResourceNotFoundException("Conseiller introuvable : " + d.conseillerId()));
    }

    private void apply(ClientPrive c, ClientDto d) {
        c.setNom(d.nom());
        c.setTel(d.tel());
        c.setEmail(d.email());
        c.setPays(d.pays());
        c.setTailles(d.tailles());
        c.setPreferences(d.preferences());
    }
}
