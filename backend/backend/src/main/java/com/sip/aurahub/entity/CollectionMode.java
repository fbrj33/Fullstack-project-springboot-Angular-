package com.sip.aurahub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Entite "collection" (nommee CollectionMode pour eviter le conflit avec java.util.Collection). */
@Entity
@Table(name = "collections")
@Getter @Setter @NoArgsConstructor
public class CollectionMode {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Saison saison;

    @Column(nullable = false)
    private Integer annee;

    private String theme;
    private LocalDate dateDefile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCollection statut = StatutCollection.EN_CONCEPTION;

    /** Date a laquelle la collection est passee EN_VENTE : sert a l'acces anticipe (RG4). */
    private LocalDateTime dateMiseEnVente;
}
