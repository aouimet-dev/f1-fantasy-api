package com.ouimet.f1.fantasy_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/f1")
@RequiredArgsConstructor
@Slf4j
public class F1Controller {

    @GetMapping
    public String home() {
        return "Si Vince ne m'aide pas, c'est une petite salope! :D";
    }
    
}
