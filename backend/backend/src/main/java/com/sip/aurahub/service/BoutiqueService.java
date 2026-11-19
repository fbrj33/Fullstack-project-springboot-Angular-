package com.sip.aurahub.service;

import com.sip.aurahub.dto.BoutiqueDto;
import com.sip.aurahub.dto.Mappers;
import com.sip.aurahub.entity.Boutique;
import com.sip.aurahub.exception.ResourceNotFoundException;
import com.sip.aurahub.repository.BoutiqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoutiqueService {
    private final BoutiqueRepository repo;

    @Transactional(readOnly = true)
    public List<BoutiqueDto> findAll() {
        return repo.findAll().stream().map(Mappers::boutiqueDto).toList();
    }

    @Transactional(readOnly = true)
    public BoutiqueDto get(Long id) {
        return Mappers.boutiqueDto(load(id));
    }

    public Boutique load(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Boutique introuvable : " + id));
    }

    @Transactional
    public BoutiqueDto create(BoutiqueDto d) {
        Boutique b = new Boutique();
        apply(b, d);
        return Mappers.boutiqueDto(repo.saveAndFlush(b));
    }

    @Transactional
    public BoutiqueDto update(Long id, BoutiqueDto d) {
        Boutique b = load(id);
        apply(b, d);
        return Mappers.boutiqueDto(repo.saveAndFlush(b));
    }

    @Transactional
    public void delete(Long id) {
        repo.delete(load(id));
        repo.flush();
    }

    private void apply(Boutique b, BoutiqueDto d) {
        b.setNom(d.nom());
        b.setVille(d.ville());
        b.setPays(d.pays());
        b.setAdresse(d.adresse());
        b.setType(d.type());
    }
}
