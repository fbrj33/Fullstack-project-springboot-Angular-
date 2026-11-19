package com.sip.aurahub.controller;

import com.sip.aurahub.dto.CollectionDto;
import com.sip.aurahub.service.CollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {
    private final CollectionService service;

    @GetMapping
    public List<CollectionDto> list() { return service.findAll(); }

    @GetMapping("/{id}")
    public CollectionDto get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CollectionDto create(@Valid @RequestBody CollectionDto d) { return service.create(d); }

    @PutMapping("/{id}")
    public CollectionDto update(@PathVariable Long id, @Valid @RequestBody CollectionDto d) { return service.update(id, d); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
