package com.sip.aurahub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Association Piece <-> Boutique portee par une quantite, par taille. */
@Entity
@Table(name = "stocks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"piece_id", "boutique_id", "taille"}))
@Getter @Setter @NoArgsConstructor
public class Stock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "piece_id")
    private Piece piece;

    @ManyToOne(optional = false)
    @JoinColumn(name = "boutique_id")
    private Boutique boutique;

    @Column(nullable = false)
    private String taille;

    @Column(nullable = false)
    private Integer quantite;
}
