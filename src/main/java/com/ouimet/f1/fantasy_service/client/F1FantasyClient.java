package com.ouimet.f1.fantasy_service.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.ouimet.f1.fantasy_service.dto.EntryResponse;
import com.ouimet.f1.fantasy_service.dto.LeagueResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class F1FantasyClient {

    private final WebClient webClient;

    public F1FantasyClient(@Value("${f1.api.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Cacheable(value = "league-data", key = "#leagueId")
    public LeagueResponse getLeague(String leagueId) {
        log.info("Fetching league: {}", leagueId);

        return webClient.get()
                .uri("/leagues/{id}", leagueId)
                .retrieve()
                .bodyToMono(LeagueResponse.class)
                .timeout(Duration.ofSeconds(10))
                .retry(3)
                .block();
    }

    @Cacheable(value = "entry-data", key = "#entryId")
    public EntryResponse getEntry(String entryId) {
        log.info("Fetching entry: {}", entryId);

        return webClient.get()
                .uri("/entry/{id}", entryId)
                .retrieve()
                .bodyToMono(EntryResponse.class)
                .timeout(Duration.ofSeconds(10))
                .retry(3)
                .block();
    }
}