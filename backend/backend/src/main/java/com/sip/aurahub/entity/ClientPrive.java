package com.sip.aurahub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "clients_prives")
@Getter @Setter @NoArgsConstructor
public class ClientPrive {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String tel;
    private String email;
    private String pays;
    private String tailles;

    @Column(length = 1000)
    private String preferences;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Niveau niveau = Niveau.PRIVILEGE;

    /** Conseiller referent (User ayant le role CONSEILLER ou ADMIN). */
    @ManyToOne(optional = false)
    @JoinColumn(name = "conseiller_id")
    private User conseiller;
}
