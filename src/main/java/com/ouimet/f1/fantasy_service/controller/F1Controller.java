package com.ouimet.f1.fantasy_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ouimet.f1.fantasy_service.client.F1FantasyClient;
import com.ouimet.f1.fantasy_service.dto.EntryResponse;
import com.ouimet.f1.fantasy_service.dto.LeagueResponse;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/f1")
@Slf4j
public class F1Controller {

    private final F1FantasyClient client;

    public F1Controller(F1FantasyClient client) {
        this.client = client;
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/league/{leagueId}")
    public ResponseEntity<LeagueResponse> getLeague(@PathVariable String leagueId) {
        return ResponseEntity.ok(client.getLeague(leagueId));
    }

    @GetMapping("/entry/{entryId}")
    public ResponseEntity<EntryResponse> getEntry(@PathVariable String entryId) {
        return ResponseEntity.ok(client.getEntry(entryId));
    }
}
