package com.sip.aurahub.dto;

import java.math.BigDecimal;
import java.util.List;

public record HistoriqueDto(ClientDto client, List<CommandeDto> commandes, BigDecimal totalAchats) {
}
