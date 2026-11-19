package com.sip.aurahub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "boutiques")
@Getter @Setter @NoArgsConstructor
public class Boutique {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String ville;

    @Column(nullable = false)
    private String pays;

    private String adresse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeBoutique type;
}
