package com.ouimet.f1.fantasy_service.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ouimet.f1.fantasy_service.dto.RaceResultDto;
import com.ouimet.f1.fantasy_service.dto.TeamPerformanceDto;
import com.ouimet.f1.fantasy_service.entity.RaceResult;
import com.ouimet.f1.fantasy_service.repository.MemberTeamRepository;
import com.ouimet.f1.fantasy_service.repository.RaceRepository;
import com.ouimet.f1.fantasy_service.repository.RaceResultRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RaceResultService {

    private final RaceResultRepository raceResultRepository;
    private final RaceRepository raceRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final StandingsService standingsService;

    @Transactional
    public void enterRaceResults(List<RaceResultDto> results, UUID raceId) {
        // Trier les résultats par points (décroissant) pour calculer les positions
        List<RaceResultDto> sortedResults = results.stream()
                .sorted((r1, r2) -> r2.getPoints().compareTo(r1.getPoints()))
                .collect(Collectors.toList());

        // Créer les entités RaceResult avec les positions calculées
        List<RaceResult> raceResultEntities = new ArrayList<>();
        for (int i = 0; i < sortedResults.size(); i++) {
            RaceResultDto result = sortedResults.get(i);
            RaceResult raceResult = new RaceResult();
            raceResult.setRace(raceRepository.findById(raceId).orElseThrow());
            raceResult.setMemberTeam(memberTeamRepository.findById(result.getMemberTeamId()).orElseThrow());
            raceResult.setPoints(result.getPoints());
            raceResult.setPosition(i + 1); // Position basée sur l'ordre trié

            raceResultEntities.add(raceResult);
        }

        raceResultRepository.saveAll(raceResultEntities);

        // Marquer la course comme complétée
        var race = raceRepository.findById(raceId).orElseThrow();
        race.setIsCompleted(true);
        raceRepository.save(race);

        // Recalculer les classements
        standingsService.recalculateStandings(raceId);
    }

    /**
     * Récupérer résultats pour une course, triés par points DESC
     */
    public List<RaceResult> getResultsByRaceIdSorted(UUID raceId) {
        log.debug("Fetching results for race {}, sorted by points", raceId);
        return raceResultRepository.findByRaceIdOrderByPointsDesc(raceId);
    }

    /**
     * Récupérer résultats pour une équipe, triés par race order (chronologically)
     */
    public List<RaceResult> getResultsByTeamIdSorted(UUID teamId) {
        log.debug("Fetching results for team {}, chronologically", teamId);
        return raceResultRepository.findByMemberTeamIdOrderByRaceNumberAsc(teamId);
    }

    /**
     * Compter résultats pour une course
     */
    public long countResultsByRaceId(UUID raceId) {
        log.debug("Counting results for race {}", raceId);
        return raceResultRepository.countByRaceId(raceId);
    }

    /**
     * Calculer performance d'une équipe
     */
    public TeamPerformanceDto calculateTeamPerformance(UUID teamId) {
        log.debug("Calculating performance for team {}", teamId);

        List<RaceResult> results = raceResultRepository.findByMemberTeamId(teamId);

        TeamPerformanceDto performance = new TeamPerformanceDto();
        performance.setTeamId(teamId);

        if (results.isEmpty()) {
            performance.setTotalPoints(0);
            performance.setRacesParticipated(0);
            performance.setAveragePointsPerRace(BigDecimal.ZERO);
            performance.setBestRacePoints(0);
            performance.setWorstRacePoints(0);
            performance.setConsistencyScore(BigDecimal.ZERO);
            return performance;
        }

        // Set team name from first result
        if (!results.isEmpty()) {
            performance.setTeamName(results.get(0).getMemberTeam().getTeamName());
        }

        int totalPoints = results.stream().mapToInt(RaceResult::getPoints).sum();
        int bestPoints = results.stream().mapToInt(RaceResult::getPoints).max().orElse(0);
        int worstPoints = results.stream().mapToInt(RaceResult::getPoints).min().orElse(0);

        performance.setTotalPoints(totalPoints);
        performance.setRacesParticipated(results.size());
        performance.setAveragePointsPerRace(
                BigDecimal.valueOf(totalPoints).divide(
                        BigDecimal.valueOf(results.size()), 2, RoundingMode.HALF_UP));
        performance.setBestRacePoints(bestPoints);
        performance.setWorstRacePoints(worstPoints);

        // Consistency score: ratio of worst to best (0-100)
        if (bestPoints > 0 && results.size() >= 2) {
            BigDecimal consistency = BigDecimal.valueOf(worstPoints)
                    .divide(BigDecimal.valueOf(bestPoints), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            performance.setConsistencyScore(consistency);
        } else {
            performance.setConsistencyScore(BigDecimal.valueOf(100));
        }

        return performance;
    }
}
