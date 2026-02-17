package com.ouimet.f1.fantasy_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ouimet.f1.fantasy_service.dto.CreateRaceDto;
import com.ouimet.f1.fantasy_service.dto.RaceDto;
import com.ouimet.f1.fantasy_service.dto.RaceResultDto;
import com.ouimet.f1.fantasy_service.entity.Race;
import com.ouimet.f1.fantasy_service.service.RaceResultService;
import com.ouimet.f1.fantasy_service.service.RaceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final RaceResultService raceResultService;
    private final RaceService raceService;

    /**
     * Entrer les résultats d'une course
     */
    @PostMapping("/races/{raceId}/results")
    public ResponseEntity<Void> enterRaceResults(
            @PathVariable UUID raceId,
            @RequestBody List<RaceResultDto> results) {

        log.info("Entering results for race {}", raceId);
        raceResultService.enterRaceResults(results, raceId);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/admin/races
     * 
     * Créer une nouvelle course (ADMIN ONLY)
     * 
     * @param createRaceDto DTO avec raceNumber, raceName, circuitName, country,
     *                      raceDate
     * @return RaceDto créée
     * @status 201 CREATED
     * @status 400 BAD REQUEST - Si données invalides ou race déjà existe
     * @throw IllegalArgumentException - Si raceNumber déjà utilisé
     */
    @PostMapping("/races")
    public ResponseEntity<RaceDto> createRace(@Valid @RequestBody CreateRaceDto createRaceDto) {
        log.info("Creating new race: {}", createRaceDto.getRaceName());

        try {
            Race savedRace = raceService.createRace(createRaceDto);
            RaceDto dto = mapToDto(savedRace);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid race creation request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Helper: convertir Race entity en RaceDto
     */
    private RaceDto mapToDto(Race race) {
        RaceDto dto = new RaceDto();
        dto.setId(race.getId());
        dto.setRaceNumber(race.getRaceNumber());
        dto.setRaceName(race.getRaceName());
        dto.setCircuitName(race.getCircuitName());
        dto.setCountry(race.getCountry());
        dto.setRaceDate(race.getRaceDate());
        dto.setIsCompleted(race.getIsCompleted());
        return dto;
    }

}
