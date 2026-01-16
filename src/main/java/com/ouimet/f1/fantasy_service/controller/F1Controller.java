package com.ouimet.f1.fantasy_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ouimet.f1.fantasy_service.properties.AuthenticationProperties;
import com.ouimet.f1.fantasy_service.service.F1AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/f1")
@RequiredArgsConstructor
@Slf4j
public class F1Controller {

    // private final F1FantasyClient f1FantasyClient;
    private final AuthenticationProperties authenticationProperties;
    private final F1AuthService f1AuthService;

    @GetMapping
    public String home() {
        return f1AuthService.authenticate(
            authenticationProperties.getUsername(),
            authenticationProperties.getPassword()
        ).toString();
    }
    
}
