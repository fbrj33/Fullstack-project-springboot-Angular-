package com.sip.aurahub.service;

import com.sip.aurahub.dto.Mappers;
import com.sip.aurahub.dto.StockDto;
import com.sip.aurahub.entity.Stock;
import com.sip.aurahub.exception.BusinessException;
import com.sip.aurahub.exception.ResourceNotFoundException;
import com.sip.aurahub.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository repo;
    private final PieceService pieces;
    private final BoutiqueService boutiques;

    @Transactional(readOnly = true)
    public List<StockDto> findAll(Long boutiqueId, Long pieceId) {
        return repo.findAll().stream()
                .filter(s -> boutiqueId == null || s.getBoutique().getId().equals(boutiqueId))
                .filter(s -> pieceId == null || s.getPiece().getId().equals(pieceId))
                .map(Mappers::stockDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockDto> faible(int seuil) {
        return repo.findByQuantiteLessThanEqual(seuil).stream().map(Mappers::stockDto).toList();
    }

    @Transactional(readOnly = true)
    public StockDto get(Long id) {
        return Mappers.stockDto(load(id));
    }

    private Stock load(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Stock introuvable : " + id));
    }

    @Transactional
    public StockDto create(StockDto d) {
        if (repo.findByPieceIdAndBoutiqueIdAndTaille(d.pieceId(), d.boutiqueId(), d.taille()).isPresent()) {
            throw new BusinessException("Une ligne de stock existe déjà pour cette pièce, cette boutique et cette taille");
        }
        Stock s = new Stock();
        apply(s, d);
        return Mappers.stockDto(repo.saveAndFlush(s));
    }

    /** Ajustement manuel du stock (ADMIN). RG1 : jamais négatif (aussi garanti par @Min(0)). */
    @Transactional
    public StockDto update(Long id, StockDto d) {
        Stock s = load(id);
        apply(s, d);
        return Mappers.stockDto(repo.saveAndFlush(s));
    }

    @Transactional
    public void delete(Long id) {
        repo.delete(load(id));
        repo.flush();
    }

    private void apply(Stock s, StockDto d) {
        if (d.quantite() < 0) throw new BusinessException("Le stock ne peut pas être négatif");
        s.setPiece(pieces.load(d.pieceId()));
        s.setBoutique(boutiques.load(d.boutiqueId()));
        s.setTaille(d.taille());
        s.setQuantite(d.quantite());
    }
}
