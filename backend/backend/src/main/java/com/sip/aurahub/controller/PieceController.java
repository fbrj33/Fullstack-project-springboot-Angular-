package com.sip.aurahub.controller;

import com.sip.aurahub.dto.DisponibiliteDto;
import com.sip.aurahub.dto.PieceDto;
import com.sip.aurahub.service.PieceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/pieces")
@RequiredArgsConstructor
public class PieceController {
    private final PieceService service;

    @GetMapping
    public List<PieceDto> list(@RequestParam(required = false) Long collectionId) { return service.findAll(collectionId); }

    @GetMapping("/{id}")
    public PieceDto get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PieceDto create(@Valid @RequestBody PieceDto d) { return service.create(d); }

    @PutMapping("/{id}")
    public PieceDto update(@PathVariable Long id, @Valid @RequestBody PieceDto d) { return service.update(id, d); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }

    /** Upload multiple : champ multipart "files". */
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PieceDto upload(@PathVariable Long id, @RequestParam("files") MultipartFile[] files) {
        return service.addImages(id, files);
    }

    @DeleteMapping("/{id}/images")
    public PieceDto deleteImage(@PathVariable Long id, @RequestParam("url") String url) {
        return service.removeImage(id, url);
    }

    /** Stock par boutique et par taille. */
    @GetMapping("/{id}/disponibilite")
    public List<DisponibiliteDto> disponibilite(@PathVariable Long id) { return service.disponibilite(id); }
}
