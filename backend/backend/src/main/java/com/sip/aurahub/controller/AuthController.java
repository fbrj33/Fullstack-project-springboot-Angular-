package com.sip.aurahub.controller;

import com.sip.aurahub.dto.AuthResponse;
import com.sip.aurahub.dto.LoginRequest;
import com.sip.aurahub.dto.RegisterRequest;
import com.sip.aurahub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest r) {
        auth.register(r);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Inscription enregistrée. Un administrateur doit valider votre compte."));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) {
        return auth.login(r);
    }
}
