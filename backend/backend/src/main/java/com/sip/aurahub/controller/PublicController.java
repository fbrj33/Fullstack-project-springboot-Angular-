package com.sip.aurahub.controller;

import com.sip.aurahub.dto.BoutiqueDto;
import com.sip.aurahub.dto.CollectionDto;
import com.sip.aurahub.dto.PiecePublicDto;
import com.sip.aurahub.service.BoutiqueService;
import com.sip.aurahub.service.CollectionService;
import com.sip.aurahub.service.PieceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Lookbook public : lecture seule, sans connexion, sans aucun prix. */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {
    private final CollectionService collections;
    private final PieceService pieces;
    private final BoutiqueService boutiques;

    @GetMapping("/collections/en-cours")
    public List<CollectionDto> enCours() {
        return collections.enCours();
    }

    @GetMapping("/collections/{id}/pieces")
    public List<PiecePublicDto> pieces(@PathVariable Long id) {
        return pieces.publicPieces(id);
    }

    @GetMapping("/boutiques")
    public List<BoutiqueDto> boutiques() {
        return boutiques.findAll();
    }
}
