package com.sip.aurahub.controller;

import com.sip.aurahub.dto.CommandeDto;
import com.sip.aurahub.dto.CommandeRequest;
import com.sip.aurahub.service.CommandeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commandes")
@RequiredArgsConstructor
public class CommandeController {
    private final CommandeService service;

    @GetMapping
    public List<CommandeDto> list() { return service.findAll(); }

    @GetMapping("/{id}")
    public CommandeDto get(@PathVariable Long id) { return service.get(id); }

    /** Crée un brouillon (vérifie RG1, RG3, RG4 mais ne décrémente pas encore le stock). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommandeDto create(@Valid @RequestBody CommandeRequest r) { return service.create(r); }

    @PutMapping("/{id}/confirmer")
    public CommandeDto confirmer(@PathVariable Long id) { return service.confirmer(id); }

    @PutMapping("/{id}/annuler")
    public CommandeDto annuler(@PathVariable Long id) { return service.annuler(id); }

    @PutMapping("/{id}/livrer")
    public CommandeDto livrer(@PathVariable Long id) { return service.livrer(id); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
