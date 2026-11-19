package com.sip.aurahub.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CommandeRequest(
        @NotNull Long clientId,
        @NotNull Long boutiqueId,
        @NotEmpty @Valid List<LigneRequest> lignes) {
}
