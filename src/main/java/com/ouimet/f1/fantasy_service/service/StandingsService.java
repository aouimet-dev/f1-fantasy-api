package com.ouimet.f1.fantasy_service.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ouimet.f1.fantasy_service.entity.Member;
import com.ouimet.f1.fantasy_service.entity.MemberStanding;
import com.ouimet.f1.fantasy_service.entity.Race;
import com.ouimet.f1.fantasy_service.entity.StandingsHistory;
import com.ouimet.f1.fantasy_service.repository.MemberRepository;
import com.ouimet.f1.fantasy_service.repository.MemberStandingRepository;
import com.ouimet.f1.fantasy_service.repository.RaceRepository;
import com.ouimet.f1.fantasy_service.repository.RaceResultRepository;
import com.ouimet.f1.fantasy_service.repository.StandingsHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class StandingsService {

    private final RaceRepository raceRepository;
    private final MemberRepository memberRepository;
    private final MemberStandingRepository memberStandingRepository;
    private final RaceResultRepository raceResultRepository;
    private final StandingsHistoryRepository standingsHistoryRepository;

    /**
     * Recalcule tous les classements après entrée de résultats
     */
    public void recalculateStandings(Long raceId) {
        log.info("Recalculating standings for race {}", raceId);

        // 1. Récupérer tous les membres
        List<Member> members = memberRepository.findAll();

        // 2. Pour chaque membre, calculer le total
        List<MemberStanding> standings = new ArrayList<>();

        for (Member member : members) {
            // Somme des points de ses 3 équipes
            Integer totalPoints = raceResultRepository.sumPointsByMember(member.getId());

            Integer racesCompleted = raceResultRepository.countCompletedRacesByMember(member.getId());

            MemberStanding standing = memberStandingRepository
                    .findByMemberId(member.getId())
                    .orElse(new MemberStanding());

            standing.setMember(member);
            standing.setTotalPoints(totalPoints);
            standing.setRacesCompleted(racesCompleted);
            standing.setAveragePointsPerRace(
                    racesCompleted > 0
                            ? BigDecimal.valueOf(totalPoints)
                                    .divide(BigDecimal.valueOf(racesCompleted), 2,
                                            RoundingMode.HALF_UP)
                            : BigDecimal.ZERO);

            standings.add(standing);
        }

        // 3. Trier par points et assigner les rangs
        standings.sort((a, b) -> b.getTotalPoints().compareTo(a.getTotalPoints()));

        for (int i = 0; i < standings.size(); i++) {
            standings.get(i).setCurrentRank(i + 1);
        }

        // 4. Sauvegarder
        memberStandingRepository.saveAll(standings);

        // 5. Sauvegarder dans l'historique
        saveStandingsHistory(raceId, standings);

        log.info("Standings recalculated successfully");
    }

    private void saveStandingsHistory(Long raceId, List<MemberStanding> standings) {
        Race race = raceRepository.findById(raceId).orElseThrow();

        for (MemberStanding standing : standings) {
            StandingsHistory history = new StandingsHistory();
            history.setRace(race);
            history.setMember(standing.getMember());
            history.setRank(standing.getCurrentRank());
            history.setTotalPoints(standing.getTotalPoints());

            // Calculer les points gagnés pour cette course
            Integer racePoints = raceResultRepository
                    .sumPointsByMemberAndRace(standing.getMember().getId(), raceId);
            history.setPointsGained(racePoints);

            // Calculer changement de rang (à implémenter)
            // history.setRankChange(calculateRankChange(...));

            standingsHistoryRepository.save(history);
        }
    }
}
