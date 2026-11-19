package com.sip.aurahub.config;

import com.sip.aurahub.entity.Categorie;
import com.sip.aurahub.entity.CollectionMode;
import com.sip.aurahub.entity.Piece;
import com.sip.aurahub.entity.Saison;
import com.sip.aurahub.entity.StatutCollection;
import com.sip.aurahub.repository.CollectionRepository;
import com.sip.aurahub.repository.PieceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("production")
@ConditionalOnProperty(name = "app.demo-lookbook.enabled", havingValue = "true")
@RequiredArgsConstructor
public class DemoLookbookInitializer implements ApplicationRunner {
    private final CollectionRepository collections;
    private final PieceRepository pieces;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (pieces.count() > 0 || !collections.findByStatut(StatutCollection.EN_VENTE).isEmpty()) {
            return;
        }

        CollectionMode collection = new CollectionMode();
        collection.setNom("Lumière de Milan");
        collection.setSaison(Saison.AUTOMNE_HIVER);
        collection.setAnnee(2026);
        collection.setTheme("Noir, ivoire et or");
        collection.setDateDefile(LocalDate.now().minusMonths(1));
        collection.setStatut(StatutCollection.EN_VENTE);
        collection.setDateMiseEnVente(LocalDateTime.now().minusDays(10));
        collection = collections.save(collection);

        pieces.saveAll(List.of(
                piece("AH26-SAC-001", "Sac Aurelia", Categorie.SAC, "Cuir de veau", "Noir", "3200.00",
                        true, 150, "d05ec2de-e4e4-40b4-8b95-f58e9b21c34b.jpg", collection),
                piece("AH26-SAC-002", "Pochette Lune", Categorie.SAC, "Satin", "Ivoire", "1450.00",
                        false, null, "ed955d89-3c05-4564-9404-19773f4cf272.jpg", collection),
                piece("AH26-ROB-001", "Robe Fiorenza", Categorie.ROBE, "Soie", "Ivoire", "2800.00",
                        false, null, "robe-fiorenza.jpeg", collection),
                piece("AH26-ROB-002", "Robe Notte", Categorie.ROBE, "Crêpe", "Noir", "2400.00",
                        false, null, "robe-notte.jpeg", collection),
                piece("AH26-MAN-001", "Manteau Duomo", Categorie.MANTEAU, "Cachemire", "Camel", "4200.00",
                        true, 80, "manteau-duomo.jpeg", collection),
                piece("AH26-SOU-001", "Escarpin Lucia", Categorie.SOULIER, "Cuir", "Noir", "980.00",
                        false, null, "escarpin-lucia.jpeg", collection),
                piece("AH26-SOU-002", "Bottine Brera", Categorie.SOULIER, "Daim", "Chocolat", "1150.00",
                        false, null, "bottine-brera.jpeg", collection),
                piece("AH26-ACC-001", "Foulard Oro", Categorie.ACCESSOIRE, "Soie", "Or", "420.00",
                        false, null, "foulard-oro.jpeg", collection)
        ));
    }

    private Piece piece(String reference, String nom, Categorie categorie, String matiere, String couleur,
                        String prix, boolean editionLimitee, Integer tirageMax, String image,
                        CollectionMode collection) {
        Piece piece = new Piece();
        piece.setReference(reference);
        piece.setNom(nom);
        piece.setCategorie(categorie);
        piece.setMatiere(matiere);
        piece.setCouleur(couleur);
        piece.setPrix(new BigDecimal(prix));
        piece.setImage("/uploads/" + image);
        piece.getImages().add("/uploads/" + image);
        piece.setEditionLimitee(editionLimitee);
        piece.setTirageMax(tirageMax);
        piece.setCollection(collection);
        return piece;
    }
}
