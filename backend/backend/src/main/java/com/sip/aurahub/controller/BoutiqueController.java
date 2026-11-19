package com.sip.aurahub.controller;

import com.sip.aurahub.dto.BoutiqueDto;
import com.sip.aurahub.service.BoutiqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boutiques")
@RequiredArgsConstructor
public class BoutiqueController {
    private final BoutiqueService service;

    @GetMapping
    public List<BoutiqueDto> list() { return service.findAll(); }

    @GetMapping("/{id}")
    public BoutiqueDto get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoutiqueDto create(@Valid @RequestBody BoutiqueDto d) { return service.create(d); }

    @PutMapping("/{id}")
    public BoutiqueDto update(@PathVariable Long id, @Valid @RequestBody BoutiqueDto d) { return service.update(id, d); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
