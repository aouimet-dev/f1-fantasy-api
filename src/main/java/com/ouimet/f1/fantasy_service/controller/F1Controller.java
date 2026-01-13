package com.ouimet.f1.fantasy_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ouimet.f1.fantasy_service.client.AuthenticationClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/f1")
@RequiredArgsConstructor
@Slf4j
public class F1Controller {

    private final AuthenticationClient authenticationClient;

    @GetMapping("/league-history")
    public Mono<String> getLeagueHistory(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(defaultValue = "12345") String leagueId) {

        return authenticationClient.getReese84CookieWithHeaders()
                .flatMap(reese84Cookie -> authenticationClient.authenticateByPasswordWithHeaders(
                        reese84Cookie,
                        username,
                        password))
                .flatMap(xF1CookieData -> authenticationClient.getLeagueHistory(leagueId, xF1CookieData));
    }

    // Endpoint pour tester uniquement l'obtention du cookie reese84
    @GetMapping("/test-reese84")
    public Mono<String> testReese84() {
        return authenticationClient.getReese84CookieWithHeaders();
    }

    // Endpoint pour tester uniquement l'authentification
    @GetMapping("/test-auth")
    public Mono<String> testAuth(
            @RequestParam String reese84Cookie,
            @RequestParam String username,
            @RequestParam String password) {

        return authenticationClient.authenticateByPasswordWithHeaders(
                reese84Cookie,
                username,
                password);
    }

}
