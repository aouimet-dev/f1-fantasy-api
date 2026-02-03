package com.ouimet.f1.fantasy_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ouimet.f1.fantasy_service.dto.RaceResultDto;
import com.ouimet.f1.fantasy_service.entity.RaceResult;
import com.ouimet.f1.fantasy_service.repository.MemberTeamRepository;
import com.ouimet.f1.fantasy_service.repository.RaceRepository;
import com.ouimet.f1.fantasy_service.repository.RaceResultRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RaceResultService {

    private final RaceResultRepository raceResultRepository;
    private final RaceRepository raceRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final StandingsService standingsService;

    @Transactional
    public void enterRaceResults(List<RaceResultDto> results, Long raceId) {
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
}
