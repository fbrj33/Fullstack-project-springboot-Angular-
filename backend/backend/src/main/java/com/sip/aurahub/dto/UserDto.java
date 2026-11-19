package com.sip.aurahub.dto;

import java.util.List;

public record UserDto(Long id, String email, String tel, String nom, String prenom, boolean enabled, List<String> roles) {
}
