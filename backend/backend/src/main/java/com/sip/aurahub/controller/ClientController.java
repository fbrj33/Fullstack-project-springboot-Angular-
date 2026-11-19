package com.sip.aurahub.controller;

import com.sip.aurahub.dto.ClientDto;
import com.sip.aurahub.dto.HistoriqueDto;
import com.sip.aurahub.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService service;

    @GetMapping
    public List<ClientDto> list() { return service.findAll(); }

    @GetMapping("/{id}")
    public ClientDto get(@PathVariable Long id) { return service.get(id); }

    @GetMapping("/{id}/historique")
    public HistoriqueDto historique(@PathVariable Long id) { return service.historique(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientDto create(@Valid @RequestBody ClientDto d) { return service.create(d); }

    @PutMapping("/{id}")
    public ClientDto update(@PathVariable Long id, @Valid @RequestBody ClientDto d) { return service.update(id, d); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
