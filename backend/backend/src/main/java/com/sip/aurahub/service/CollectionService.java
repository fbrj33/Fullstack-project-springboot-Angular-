package com.sip.aurahub.service;

import com.sip.aurahub.dto.CollectionDto;
import com.sip.aurahub.dto.Mappers;
import com.sip.aurahub.entity.CollectionMode;
import com.sip.aurahub.entity.StatutCollection;
import com.sip.aurahub.exception.ResourceNotFoundException;
import com.sip.aurahub.repository.CollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectionService {
    private final CollectionRepository repo;

    @Transactional(readOnly = true)
    public List<CollectionDto> findAll() {
        return repo.findAll().stream().map(Mappers::collectionDto).toList();
    }

    @Transactional(readOnly = true)
    public CollectionDto get(Long id) {
        return Mappers.collectionDto(load(id));
    }

    /** Collections actuellement EN_VENTE (lookbook public). */
    @Transactional(readOnly = true)
    public List<CollectionDto> enCours() {
        return repo.findByStatut(StatutCollection.EN_VENTE).stream().map(Mappers::collectionDto).toList();
    }

    public CollectionMode load(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Collection introuvable : " + id));
    }

    @Transactional
    public CollectionDto create(CollectionDto d) {
        CollectionMode c = new CollectionMode();
        apply(c, d);
        return Mappers.collectionDto(repo.saveAndFlush(c));
    }

    @Transactional
    public CollectionDto update(Long id, CollectionDto d) {
        CollectionMode c = load(id);
        apply(c, d);
        return Mappers.collectionDto(repo.saveAndFlush(c));
    }

    @Transactional
    public void delete(Long id) {
        repo.delete(load(id));
        repo.flush();
    }

    private void apply(CollectionMode c, CollectionDto d) {
        c.setNom(d.nom());
        c.setSaison(d.saison());
        c.setAnnee(d.annee());
        c.setTheme(d.theme());
        c.setDateDefile(d.dateDefile());
        StatutCollection nouveau = d.statut() == null ? StatutCollection.EN_CONCEPTION : d.statut();
        // RG3/RG4 : on retient l'instant du passage EN_VENTE (début de l'accès anticipé)
        if (nouveau == StatutCollection.EN_VENTE && c.getStatut() != StatutCollection.EN_VENTE) {
            c.setDateMiseEnVente(LocalDateTime.now());
        }
        c.setStatut(nouveau);
    }
}
