package com.ouimet.f1.fantasy_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ouimet.f1.fantasy_service.dto.RaceResultDetailDto;
import com.ouimet.f1.fantasy_service.dto.TeamPerformanceDto;
import com.ouimet.f1.fantasy_service.entity.RaceResult;
import com.ouimet.f1.fantasy_service.service.RaceResultService;
import com.ouimet.f1.fantasy_service.service.MemberTeamService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller pour récupérer résultats détaillés
 * Endpoints: GET /api/members/{memberId}/teams/{teamId}/results
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class RaceResultController {

    private final RaceResultService raceResultService;
    private final MemberTeamService memberTeamService;

    /**
     * GET /api/members/{memberId}/teams/{teamId}/results
     * 
     * Récupère tous les résultats d'une équipe spécifique
     * Triés par race order chronologique
     * 
     * @param memberId ID du membre
     * @param teamId   ID de l'équipe
     * @return List<RaceResultDetailDto> tous les résultats
     * @status 200 OK - Si équipe existe (même si zéro résultats)
     * @status 404 NOT FOUND - Si équipe pas trouvée
     * 
     *         Example Response:
     *         [
     *         {
     *         "raceResultId": 101,
     *         "raceId": 1,
     *         "raceName": "Australian GP",
     *         "memberTeamId": 5,
     *         "teamName": "Red Bulls",
     *         "memberName": "Alice",
     *         "points": 120,
     *         "position": 2,
     *         "enteredAt": "2026-03-16T10:30:00"
     *         },
     *         ...
     *         ]
     */
    @GetMapping("/api/members/{memberId}/teams/{teamId}/results")
    public ResponseEntity<List<RaceResultDetailDto>> getTeamResults(
            @PathVariable UUID memberId,
            @PathVariable UUID teamId) {

        log.debug("Fetching results for team {} of member {}", teamId, memberId);

        // Vérifier que team existe et appartient à ce member
        if (memberTeamService.getTeamByMemberAndTeamId(memberId, teamId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<RaceResult> results = raceResultService.getResultsByTeamIdSorted(teamId);
        List<RaceResultDetailDto> dtos = results.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/members/{memberId}/teams/{teamId}/performance
     * 
     * Récupère les statistiques de performance d'une équipe
     * 
     * @param memberId ID du membre
     * @param teamId   ID de l'équipe
     * @return TeamPerformanceDto
     * @status 200 OK
     * @status 404 NOT FOUND
     */
    @GetMapping("/api/members/{memberId}/teams/{teamId}/performance")
    public ResponseEntity<TeamPerformanceDto> getTeamPerformance(
            @PathVariable UUID memberId,
            @PathVariable UUID teamId) {

        log.debug("Fetching performance stats for team {} of member {}", teamId, memberId);

        if (memberTeamService.getTeamByMemberAndTeamId(memberId, teamId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TeamPerformanceDto performance = raceResultService.calculateTeamPerformance(teamId);
        return ResponseEntity.ok(performance);
    }

    /**
     * Helper: convertir RaceResult entity en RaceResultDetailDto
     */
    private RaceResultDetailDto mapToDto(RaceResult result) {
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
