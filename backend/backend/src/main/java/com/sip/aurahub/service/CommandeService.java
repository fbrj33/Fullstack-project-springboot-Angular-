package com.sip.aurahub.service;

import com.sip.aurahub.dto.CommandeDto;
import com.sip.aurahub.dto.CommandeRequest;
import com.sip.aurahub.dto.LigneRequest;
import com.sip.aurahub.dto.Mappers;
import com.sip.aurahub.entity.*;
import com.sip.aurahub.exception.BusinessException;
import com.sip.aurahub.exception.ResourceNotFoundException;
import com.sip.aurahub.repository.*;
import com.sip.aurahub.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Coeur métier : RG1 (stock jamais négatif), RG2 (tirage + numéro d'exemplaire), RG3 (collection EN_VENTE),
 * RG4 (accès anticipé), RG6 (décrément à la confirmation / restitution à l'annulation), RG7 (niveau client).
 * Toute exception métier annule la transaction : une commande refusée ne laisse aucune trace partielle.
 */
@Service
@RequiredArgsConstructor
public class CommandeService {
    private final CommandeRepository commandes;
    private final BoutiqueRepository boutiques;
    private final PieceRepository pieces;
    private final StockRepository stocks;
    private final LigneRepository lignes;
    private final ClientService clientService;
    private final CurrentUser currentUser;

    @Value("${app.early-access-hours:48}")
    private long heuresAccesAnticipe;

    // ------------------------------------------------------------------ lecture

    @Transactional(readOnly = true)
    public List<CommandeDto> findAll() {
        List<Commande> list = currentUser.isAdmin()
                ? commandes.findAllByOrderByDateCommandeDesc()
                : commandes.findByClientConseillerIdOrderByDateCommandeDesc(currentUser.get().getId());   // RG5
        return list.stream().map(Mappers::commandeDto).toList();
    }

    @Transactional(readOnly = true)
    public CommandeDto get(Long id) {
        return Mappers.commandeDto(loadAuthorized(id));
    }

    // ------------------------------------------------------------------ création (brouillon)

    @Transactional
    public CommandeDto create(CommandeRequest r) {
        ClientPrive client = clientService.loadAuthorized(r.clientId());               // RG5
        Boutique boutique = boutiques.findById(r.boutiqueId())
                .orElseThrow(() -> new ResourceNotFoundException("Boutique introuvable : " + r.boutiqueId()));

        Commande c = new Commande();
        c.setClient(client);
        c.setBoutique(boutique);
        c.setDateCommande(LocalDateTime.now());
        c.setStatut(StatutCommande.BROUILLON);

        BigDecimal total = BigDecimal.ZERO;
        for (LigneRequest lr : r.lignes()) {
            Piece p = pieces.findById(lr.pieceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pièce introuvable : " + lr.pieceId()));
            verifierCommandable(p, client);                                              // RG3 + RG4
            if (p.isEditionLimitee() && lr.quantite() != 1) {
                throw new BusinessException("« " + p.getNom() + " » est une édition limitée : 1 exemplaire numéroté par ligne");
            }
            Stock s = stocks.findByPieceIdAndBoutiqueIdAndTaille(p.getId(), boutique.getId(), lr.taille())
                    .orElseThrow(() -> new BusinessException("Aucun stock de « " + p.getNom() + " » en taille "
                            + lr.taille() + " dans cette boutique"));
            if (s.getQuantite() < lr.quantite()) {                                        // RG1 (contrôle anticipé)
                throw new BusinessException("Stock insuffisant pour « " + p.getNom() + " » (" + lr.taille()
                        + ") : " + s.getQuantite() + " disponible(s)");
            }
            LigneCommande l = new LigneCommande();
            l.setCommande(c);
            l.setPiece(p);
            l.setTaille(lr.taille());
            l.setQuantite(lr.quantite());
            l.setPrixUnitaire(p.getPrix());
            c.getLignes().add(l);
            total = total.add(p.getPrix().multiply(BigDecimal.valueOf(lr.quantite())));
        }
        c.setTotal(total);
        return Mappers.commandeDto(commandes.saveAndFlush(c));
    }

    // ------------------------------------------------------------------ confirmation

    @Transactional
    public CommandeDto confirmer(Long id) {
        Commande c = loadAuthorized(id);
        if (c.getStatut() != StatutCommande.BROUILLON) {
            throw new BusinessException("Seule une commande en brouillon peut être confirmée");
        }
        for (LigneCommande l : c.getLignes()) {
            Piece p = l.getPiece();
            if (p.isEditionLimitee()) {
                // verrou sur la pièce : deux ventes simultanées n'obtiennent pas le même numéro
                p = pieces.findByIdForUpdate(p.getId()).orElseThrow();
            }
            verifierCommandable(p, c.getClient());                                       // RG3 + RG4

            Stock s = stocks.findForUpdate(p.getId(), c.getBoutique().getId(), l.getTaille())
                    .orElseThrow(() -> new BusinessException("Plus de stock pour « " + l.getPiece().getNom()
                            + " » (" + l.getTaille() + ") dans cette boutique"));
            if (s.getQuantite() < l.getQuantite()) {                                      // RG1
                throw new BusinessException("Stock insuffisant pour « " + l.getPiece().getNom() + " » ("
                        + l.getTaille() + ") : " + s.getQuantite() + " disponible(s)");
            }
            s.setQuantite(s.getQuantite() - l.getQuantite());                             // RG6

            if (p.isEditionLimitee()) {                                                   // RG2
                l.setNumeroExemplaire(prochainNumero(p));
            }
        }
        c.setStatut(StatutCommande.CONFIRMEE);
        commandes.flush();
        clientService.recalculerNiveau(c.getClient());                                    // RG7
        return Mappers.commandeDto(c);
    }

    // ------------------------------------------------------------------ annulation / livraison

    @Transactional
    public CommandeDto annuler(Long id) {
        Commande c = loadAuthorized(id);
        if (c.getStatut() == StatutCommande.ANNULEE || c.getStatut() == StatutCommande.LIVREE) {
            throw new BusinessException("Une commande " + c.getStatut() + " ne peut plus être annulée");
        }
        if (c.getStatut() == StatutCommande.CONFIRMEE) {
            for (LigneCommande l : c.getLignes()) {
                Stock s = stocks.findForUpdate(l.getPiece().getId(), c.getBoutique().getId(), l.getTaille())
                        .orElseGet(() -> {
                            Stock n = new Stock();
                            n.setPiece(l.getPiece());
                            n.setBoutique(c.getBoutique());
                            n.setTaille(l.getTaille());
                            n.setQuantite(0);
                            return n;
                        });
                s.setQuantite(s.getQuantite() + l.getQuantite());                         // RG6 : restitution
                stocks.save(s);
                l.setNumeroExemplaire(null);                                              // le numéro redevient disponible
            }
        }
        c.setStatut(StatutCommande.ANNULEE);
        commandes.flush();
        clientService.recalculerNiveau(c.getClient());                                    // RG7
        return Mappers.commandeDto(c);
    }

    @Transactional
    public CommandeDto livrer(Long id) {
        Commande c = loadAuthorized(id);
        if (c.getStatut() != StatutCommande.CONFIRMEE) {
            throw new BusinessException("Seule une commande confirmée peut être livrée");
        }
        c.setStatut(StatutCommande.LIVREE);
        return Mappers.commandeDto(c);
    }

    @Transactional
    public void delete(Long id) {
        Commande c = loadAuthorized(id);
        if (c.getStatut() != StatutCommande.BROUILLON) {
            throw new BusinessException("Seul un brouillon peut être supprimé (annulez la commande sinon)");
        }
        commandes.delete(c);
    }

    // ------------------------------------------------------------------ règles

    private Commande loadAuthorized(Long id) {
        Commande c = commandes.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable : " + id));
        clientService.checkAccess(c.getClient());                                         // RG5
        return c;
    }

    /** RG3 : collection EN_VENTE ; RG4 : édition limitée réservée PRESTIGE/ELITE pendant l'accès anticipé. */
    private void verifierCommandable(Piece p, ClientPrive client) {
        CollectionMode col = p.getCollection();
        if (col.getStatut() != StatutCollection.EN_VENTE) {
            throw new BusinessException("« " + p.getNom() + " » n'est pas commandable : collection "
                    + col.getStatut());
        }
        if (p.isEditionLimitee() && accesAnticipeActif(col) && client.getNiveau() == Niveau.PRIVILEGE) {
            throw new BusinessException("« " + p.getNom() + " » : accès anticipé de " + heuresAccesAnticipe
                    + " h réservé aux clients PRESTIGE et ELITE");
        }
    }

    private boolean accesAnticipeActif(CollectionMode col) {
        return col.getDateMiseEnVente() != null
                && LocalDateTime.now().isBefore(col.getDateMiseEnVente().plusHours(heuresAccesAnticipe));
    }

    /** RG2 : plus petit numéro libre entre 1 et tirageMax ; erreur si le tirage est épuisé. */
    private int prochainNumero(Piece p) {
        Set<Integer> utilises = new HashSet<>(lignes.numerosUtilises(p.getId()));
        for (int n = 1; n <= p.getTirageMax(); n++) {
            if (!utilises.contains(n)) return n;
        }
        throw new BusinessException("Tirage épuisé pour « " + p.getNom() + " » (" + p.getTirageMax() + " exemplaires)");
    }
}
