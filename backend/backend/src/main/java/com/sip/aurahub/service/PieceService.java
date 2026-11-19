package com.sip.aurahub.service;

import com.sip.aurahub.dto.DisponibiliteDto;
import com.sip.aurahub.dto.Mappers;
import com.sip.aurahub.dto.PieceDto;
import com.sip.aurahub.dto.PiecePublicDto;
import com.sip.aurahub.entity.CollectionMode;
import com.sip.aurahub.entity.Piece;
import com.sip.aurahub.entity.StatutCollection;
import com.sip.aurahub.exception.BusinessException;
import com.sip.aurahub.exception.ResourceNotFoundException;
import com.sip.aurahub.repository.PieceRepository;
import com.sip.aurahub.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PieceService {
    private final PieceRepository repo;
    private final StockRepository stocks;
    private final CollectionService collections;
    private final FileStorageService storage;

    @Transactional(readOnly = true)
    public List<PieceDto> findAll(Long collectionId) {
        List<Piece> list = collectionId == null ? repo.findAll() : repo.findByCollectionId(collectionId);
        return list.stream().map(Mappers::pieceDto).toList();
    }

    @Transactional(readOnly = true)
    public PieceDto get(Long id) {
        return Mappers.pieceDto(load(id));
    }

    public Piece load(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pièce introuvable : " + id));
    }

    @Transactional
    public PieceDto create(PieceDto d) {
        Piece p = new Piece();
        apply(p, d);
        return Mappers.pieceDto(repo.saveAndFlush(p));
    }

    @Transactional
    public PieceDto update(Long id, PieceDto d) {
        Piece p = load(id);
        apply(p, d);
        return Mappers.pieceDto(repo.saveAndFlush(p));
    }

    @Transactional
    public void delete(Long id) {
        Piece p = load(id);
        List<String> urls = List.copyOf(p.getImages());
        repo.delete(p);
        repo.flush();
        urls.forEach(storage::delete);
    }

    /** Upload de plusieurs visuels : fichiers sur disque, URLs en base. */
    @Transactional
    public PieceDto addImages(Long id, MultipartFile[] files) {
        Piece p = load(id);
        for (MultipartFile f : files) {
            if (f.isEmpty()) continue;
            String url = storage.store(f);
            p.getImages().add(url);
            if (p.getImage() == null || p.getImage().isBlank()) p.setImage(url);
        }
        return Mappers.pieceDto(repo.saveAndFlush(p));
    }

    @Transactional
    public PieceDto removeImage(Long id, String url) {
        Piece p = load(id);
        if (!p.getImages().remove(url)) throw new ResourceNotFoundException("Image introuvable sur cette pièce");
        if (url.equals(p.getImage())) {
            p.setImage(p.getImages().isEmpty() ? null : p.getImages().get(0));
        }
        storage.delete(url);
        return Mappers.pieceDto(repo.saveAndFlush(p));
    }

    /** Stock par boutique et par taille. */
    @Transactional(readOnly = true)
    public List<DisponibiliteDto> disponibilite(Long id) {
        load(id);
        return stocks.findByPieceId(id).stream()
                .map(s -> new DisponibiliteDto(s.getBoutique().getId(), s.getBoutique().getNom(),
                        s.getBoutique().getVille(), s.getTaille(), s.getQuantite()))
                .toList();
    }

    /** Lookbook public : pièces d'une collection EN_VENTE, sans prix. */
    @Transactional(readOnly = true)
    public List<PiecePublicDto> publicPieces(Long collectionId) {
        CollectionMode c = collections.load(collectionId);
        if (c.getStatut() != StatutCollection.EN_VENTE) {
            throw new ResourceNotFoundException("Collection non disponible");
        }
        return repo.findByCollectionId(collectionId).stream().map(Mappers::piecePublic).toList();
    }

    private void apply(Piece p, PieceDto d) {
        if (d.editionLimitee() && (d.tirageMax() == null || d.tirageMax() < 1)) {
            throw new BusinessException("tirageMax (> 0) est obligatoire pour une édition limitée");
        }
        p.setReference(d.reference());
        p.setNom(d.nom());
        p.setCategorie(d.categorie());
        p.setMatiere(d.matiere());
        p.setCouleur(d.couleur());
        p.setPrix(d.prix());
        if (d.image() != null) p.setImage(d.image());
        p.setEditionLimitee(d.editionLimitee());
        p.setTirageMax(d.editionLimitee() ? d.tirageMax() : null);
        p.setCollection(collections.load(d.collectionId()));
    }
}
