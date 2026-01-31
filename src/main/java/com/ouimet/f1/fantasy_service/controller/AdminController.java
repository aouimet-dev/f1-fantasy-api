package com.ouimet.f1.fantasy_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ouimet.f1.fantasy_service.dto.RaceResultDto;
import com.ouimet.f1.fantasy_service.service.RaceResultService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final RaceResultService raceResultService;

    /**
     * Entrer les résultats d'une course
     */
    @PostMapping("/races/{raceId}/results")
    public ResponseEntity<Void> enterRaceResults(
            @PathVariable Long raceId,
            @RequestBody List<RaceResultDto> results) {

        log.info("Entering results for race {}", raceId);
        raceResultService.enterRaceResults(results, raceId);
        return ResponseEntity.ok().build();
    }

}
