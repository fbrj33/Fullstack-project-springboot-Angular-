# Diagramme de classes - Aura Hub

Coller le bloc ci-dessous sur https://mermaid.live pour l'exporter en PNG/SVG (livrable demandé).

```mermaid
classDiagram
    direction LR
    class User {
        +Long id
        +String email
        +String password
        +String tel
        +String nom
        +String prenom
        +boolean enabled
    }
    class Role {
        +Long id
        +RoleName libelle
    }
    class CollectionMode {
        +Long id
        +String nom
        +Saison saison
        +Integer annee
        +String theme
        +LocalDate dateDefile
        +StatutCollection statut
        +LocalDateTime dateMiseEnVente
    }
    class Piece {
        +Long id
        +String reference
        +String nom
        +Categorie categorie
        +String matiere
        +String couleur
        +BigDecimal prix
        +String image
        +List~String~ images
        +boolean editionLimitee
        +Integer tirageMax
    }
    class Boutique {
        +Long id
        +String nom
        +String ville
        +String pays
        +String adresse
        +TypeBoutique type
    }
    class Stock {
        +Long id
        +String taille
        +Integer quantite
    }
    class ClientPrive {
        +Long id
        +String nom
        +String tel
        +String email
        +String pays
        +String tailles
        +String preferences
        +Niveau niveau
    }
    class Commande {
        +Long id
        +LocalDateTime date
        +StatutCommande statut
        +BigDecimal total
    }
    class LigneCommande {
        +Long id
        +String taille
        +Integer quantite
        +BigDecimal prixUnitaire
        +Integer numeroExemplaire
    }

    User "*" -- "*" Role : user_roles
    CollectionMode "1" --> "*" Piece
    Piece "1" --> "*" Stock
    Boutique "1" --> "*" Stock
    User "1" --> "*" ClientPrive : conseiller
    ClientPrive "1" --> "*" Commande
    Boutique "1" --> "*" Commande
    Commande "1" *-- "*" LigneCommande
    LigneCommande "*" --> "1" Piece
```

`Stock` porte l'association Many-to-Many **Piece <-> Boutique** (quantité par taille et par boutique).
Énumérations : RoleName (ADMIN, CONSEILLER), Saison, StatutCollection, Categorie, TypeBoutique, Niveau, StatutCommande.
