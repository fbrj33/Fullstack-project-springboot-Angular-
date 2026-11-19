package com.sip.aurahub.dto;

import com.sip.aurahub.entity.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record UserRequest(
        @NotBlank @Email String email,
        String password,
        String tel,
        @NotBlank String nom,
        @NotBlank String prenom,
        Set<RoleName> roles,
        Boolean enabled) {
}
