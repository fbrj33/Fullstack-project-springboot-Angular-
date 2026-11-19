package com.sip.aurahub.controller;

import com.sip.aurahub.dto.Mappers;
import com.sip.aurahub.dto.UserDto;
import com.sip.aurahub.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MeController {
    private final CurrentUser currentUser;

    /** Profil de l'utilisateur connecté (utile à Angular pour restaurer la session). */
    @GetMapping("/api/me")
    public UserDto me() {
        return Mappers.userDto(currentUser.get());
    }
}
