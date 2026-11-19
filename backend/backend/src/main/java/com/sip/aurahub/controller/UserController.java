package com.sip.aurahub.controller;

import com.sip.aurahub.dto.UserDto;
import com.sip.aurahub.dto.UserRequest;
import com.sip.aurahub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping
    public List<UserDto> list() { return service.findAll(); }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserRequest r) { return service.create(r); }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable Long id, @Valid @RequestBody UserRequest r) { return service.update(id, r); }

    /** Validation d'un compte après inscription. */
    @PutMapping("/{id}/activer")
    public UserDto activer(@PathVariable Long id) { return service.activer(id); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
