package com.sip.aurahub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pieces")
@Getter @Setter @NoArgsConstructor
public class Piece {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categorie categorie;

    private String matiere;
    private String couleur;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    /** Visuel principal. */
    private String image;

    /** Galerie : URLs des visuels (plusieurs images par piece). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "piece_images", joinColumns = @JoinColumn(name = "piece_id"))
    @Column(name = "url")
    private List<String> images = new ArrayList<>();

    private boolean editionLimitee;
    private Integer tirageMax;

    @ManyToOne(optional = false)
    @JoinColumn(name = "collection_id")
    private CollectionMode collection;
}
