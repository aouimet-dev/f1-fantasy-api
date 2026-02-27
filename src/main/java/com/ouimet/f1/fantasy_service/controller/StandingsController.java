package com.ouimet.f1.fantasy_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ouimet.f1.fantasy_service.dto.StandingDto;
import com.ouimet.f1.fantasy_service.entity.Standing;
import com.ouimet.f1.fantasy_service.service.StandingsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller pour gérer les classements
 * Endpoints: GET /api/standings
 */
@RestController
@RequestMapping("/api/standings")
@RequiredArgsConstructor
@Slf4j
public class StandingsController {

    private final StandingsService standingsService;

    /**
     * GET /api/standings
     * 
     * Récupère TOUS les standings triés par rank ASC
     * 
     * @return List<StandingDto> triée par currentRank
     * @status 200 OK - Always returns, at least empty list
     * 
     *         Example Response:
     *         [
     *         {
     *         "memberId": 1,
     *         "memberName": "Alice",
     *         "totalPoints": 450,
     *         "racesCompleted": 5,
     *         "averagePointsPerRace": 90.0,
     *         "currentRank": 1,
     *         "lastRacePoints": 95,
     *         "pointsChangePercent": 5.3,
     *         "rankChange": 1
     *         },
     *         ...
     *         ]
     */
    @GetMapping
    public ResponseEntity<List<StandingDto>> getAllStandings() {
        log.debug("Fetching all standings");
        List<Standing> standings = standingsService.getAllStandingsSorted();
        List<StandingDto> dtos = standings.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/standings/{memberId}
     * 
     * Récupère le standing d'un membre spécifique
     * 
     * @param memberId ID du membre
     * @return StandingDto du membre
     * @status 200 OK - Si standing existe
     * @status 404 NOT FOUND - Si aucun standing pour ce member
     */
    @GetMapping("/{memberId}")
    public ResponseEntity<StandingDto> getMemberStanding(@PathVariable UUID memberId) {
        log.debug("Fetching standing for member {}", memberId);
        return standingsService.getStandingByMemberId(memberId)
                .map(standing -> ResponseEntity.ok(mapToDto(standing)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Helper: convertir Standing entity en StandingDto
     */
    private StandingDto mapToDto(Standing standing) {
        StandingDto dto = new StandingDto();
        dto.setMemberId(standing.getMember().getId());
        dto.setMemberName(standing.getMember().getName());
        dto.setTotalPoints(standing.getTotalPoints());
        dto.setRacesCompleted(standing.getRacesCompleted());
        dto.setAveragePointsPerRace(standing.getAveragePointsPerRace());
        dto.setCurrentRank(standing.getCurrentRank());
        dto.setLastRacePoints(standing.getLastRacePoints());
        // rankChange et pointsChangePercent à calculer via service helper si nécessaire
        return dto;
    }
}
