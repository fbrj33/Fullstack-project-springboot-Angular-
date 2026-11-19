package com.sip.aurahub.controller;

import com.sip.aurahub.dto.StatsDto;
import com.sip.aurahub.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService service;

    @GetMapping
    public StatsDto stats() { return service.stats(); }
}
