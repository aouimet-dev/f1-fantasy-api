package com.ouimet.f1.fantasy_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ouimet.f1.fantasy_service.dto.RaceDto;
import com.ouimet.f1.fantasy_service.dto.RaceResultDetailDto;
import com.ouimet.f1.fantasy_service.entity.Race;
import com.ouimet.f1.fantasy_service.entity.RaceResult;
import com.ouimet.f1.fantasy_service.service.RaceService;
import com.ouimet.f1.fantasy_service.service.RaceResultService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller pour gérer les courses
 * Endpoints: GET /api/races
 */
@RestController
@RequestMapping("/api/races")
@RequiredArgsConstructor
@Slf4j
public class RaceController {

    private final RaceService raceService;
    private final RaceResultService raceResultService;

    /**
     * GET /api/races
     * 
     * Récupère TOUTES les courses triées par raceNumber ASC
     * Indique si complétées ou non
     * 
     * @return List<RaceDto> triée par raceNumber
     * @status 200 OK - Always returns, at least empty list
     * 
     *         Example Response:
     *         [
     *         {
     *         "raceId": 1,
     *         "raceNumber": 1,
     *         "raceName": "Australian Grand Prix",
     *         "circuitName": "Melbourne",
     *         "country": "Australia",
     *         "raceDate": "2026-03-15",
     *         "isCompleted": true,
     *         "resultCount": 3
     *         },
     *         {
     *         "raceId": 2,
     *         "raceNumber": 2,
     *         "raceName": "Saudi Arabian Grand Prix",
     *         "circuitName": "Jeddah",
     *         "country": "Saudi Arabia",
     *         "raceDate": "2026-03-20",
     *         "isCompleted": false,
     *         "resultCount": 0
     *         },
     *         ...
     *         ]
     */
    @GetMapping
    public ResponseEntity<List<RaceDto>> getAllRaces() {
        log.debug("Fetching all races");
        List<Race> races = raceService.getAllRacesSorted();
        List<RaceDto> dtos = races.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/races?completed={boolean}
     * 
     * Récupère les courses filtrées par statut completion
     * 
     * @param completed true=complétées, false=à venir
     * @return List<RaceDto>
     * @status 200 OK
     */
    @GetMapping(params = "completed")
    public ResponseEntity<List<RaceDto>> getRacesByStatus(@RequestParam Boolean completed) {
        log.debug("Fetching races by status: completed={}", completed);
        List<Race> races = raceService.getRacesByCompleted(completed);
        List<RaceDto> dtos = races.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/races/{raceId}
     * 
     * Récupère les détails d'une course spécifique
     * 
     * @param raceId ID de la course
     * @return RaceDto
     * @status 200 OK - Si race existe
     * @status 404 NOT FOUND - Si race pas trouvée
     */
    @GetMapping("/{raceId}")
    public ResponseEntity<RaceDto> getRaceById(@PathVariable UUID raceId) {
        log.debug("Fetching race {}", raceId);
        return raceService.getRaceById(raceId)
                .map(race -> ResponseEntity.ok(mapToDto(race)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/races/{raceId}/results
     * 
     * Récupère les résultats d'une course
     * Triés par points DESC (meilleur score en premier)
     * 
     * @param raceId ID de la course
     * @return List<RaceResultDetailDto>
     * @status 200 OK - Si race existe (même si zéro résultats)
     * @status 404 NOT FOUND - Si race pas trouvée
     */
    @GetMapping("/{raceId}/results")
    public ResponseEntity<List<RaceResultDetailDto>> getRaceResults(@PathVariable UUID raceId) {
        log.debug("Fetching results for race {}", raceId);

        // Vérifier que la course existe
        if (raceService.getRaceById(raceId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<RaceResult> results = raceResultService.getResultsByRaceIdSorted(raceId);
        List<RaceResultDetailDto> dtos = results.stream()
                .map(this::mapResultToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
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
        // resultCount: compter results pour cette race
        dto.setResultCount((int) raceResultService.countResultsByRaceId(race.getId()));
        return dto;
    }

    /**
     * Helper: convertir RaceResult entity en RaceResultDetailDto
     */
    private RaceResultDetailDto mapResultToDto(RaceResult result) {
        RaceResultDetailDto dto = new RaceResultDetailDto();
        dto.setRaceResultId(result.getId());
        dto.setRaceId(result.getRace().getId());
        dto.setRaceName(result.getRace().getRaceName());
        dto.setMemberTeamId(result.getMemberTeam().getId());
        dto.setTeamName(result.getMemberTeam().getTeamName());
        dto.setMemberName(result.getMemberTeam().getMember().getName());
        dto.setPoints(result.getPoints());
        dto.setPosition(result.getPosition());
        dto.setEnteredAt(result.getEnteredAt());
        return dto;
    }
}
