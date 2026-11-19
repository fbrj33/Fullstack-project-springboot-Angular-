package com.sip.aurahub.controller;

import com.sip.aurahub.dto.StockDto;
import com.sip.aurahub.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {
    private final StockService service;

    @GetMapping
    public List<StockDto> list(@RequestParam(required = false) Long boutiqueId,
                               @RequestParam(required = false) Long pieceId) {
        return service.findAll(boutiqueId, pieceId);
    }

    /** Lignes de stock faible (quantité <= seuil) pour le tableau de bord. */
    @GetMapping("/faible")
    public List<StockDto> faible(@RequestParam(defaultValue = "3") int seuil) { return service.faible(seuil); }

    @GetMapping("/{id}")
    public StockDto get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockDto create(@Valid @RequestBody StockDto d) { return service.create(d); }

    @PutMapping("/{id}")
    public StockDto update(@PathVariable Long id, @Valid @RequestBody StockDto d) { return service.update(id, d); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
