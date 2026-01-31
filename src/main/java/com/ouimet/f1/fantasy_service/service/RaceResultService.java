package com.ouimet.f1.fantasy_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ouimet.f1.fantasy_service.dto.RaceResultDto;
import com.ouimet.f1.fantasy_service.entity.RaceResult;
import com.ouimet.f1.fantasy_service.repository.MemberTeamRepository;
import com.ouimet.f1.fantasy_service.repository.RaceRepository;
import com.ouimet.f1.fantasy_service.repository.RaceResultRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RaceResultService {

    private final RaceResultRepository raceResultRepository;
    private final RaceRepository raceRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final StandingsService standingsService;
    
    public void enterRaceResults(List<RaceResultDto> results, Long raceId) {
        for (RaceResultDto result : results) {
            RaceResult raceResult = new RaceResult();
            raceResult.setRace(raceRepository.findById(raceId).orElseThrow());
            raceResult.setMemberTeam(memberTeamRepository.findById(result.getMemberTeamId()).orElseThrow());
            raceResult.setPoints(result.getPoints());
            raceResult.setPosition(result.getPosition());

            raceResultRepository.save(raceResult);
        }

        // Recalculer les classements
        standingsService.recalculateStandings(raceId);
    }
}
