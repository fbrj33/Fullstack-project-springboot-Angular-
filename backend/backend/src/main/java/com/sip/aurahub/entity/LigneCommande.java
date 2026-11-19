package com.sip.aurahub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "lignes_commande")
@Getter @Setter @NoArgsConstructor
public class LigneCommande {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "commande_id")
    private Commande commande;

    @ManyToOne(optional = false)
    @JoinColumn(name = "piece_id")
    private Piece piece;

    @Column(nullable = false)
    private String taille;

    @Column(nullable = false)
    private Integer quantite;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    /** Numero d'exemplaire (ex. 37 pour 37/150) - uniquement pour les editions limitees (RG2). */
    private Integer numeroExemplaire;
}
