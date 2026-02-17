package com.ouimet.f1.fantasy_service.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ouimet.f1.fantasy_service.entity.Member;
import com.ouimet.f1.fantasy_service.entity.Race;
import com.ouimet.f1.fantasy_service.entity.Standing;
import com.ouimet.f1.fantasy_service.entity.StandingsHistory;
import com.ouimet.f1.fantasy_service.repository.MemberRepository;
import com.ouimet.f1.fantasy_service.repository.RaceRepository;
import com.ouimet.f1.fantasy_service.repository.RaceResultRepository;
import com.ouimet.f1.fantasy_service.repository.StandingRepository;
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
    private final StandingRepository standingRepository;
    private final RaceResultRepository raceResultRepository;
    private final StandingsHistoryRepository standingsHistoryRepository;

    /**
     * Recalcule le classement global de la saison pour TOUS les membres
     * et crée un snapshot historique après une course donnée.
     *
     * Cette méthode :
     * 1. Calcule pour chaque membre : points totaux, nombre de courses complétées,
     * moyenne points/course
     * 2. Assigne les rangs en triant par points totaux (ordre décroissant)
     * 3. Sauvegarde le classement global mis à jour
     * 4. Enregistre un historique (snapshot) du classement à ce moment précis,
     * incluant les points gagnés dans la course
     *
     * @param completedRaceId l'identifiant de la course qui vient d'être complétée
     *                        (pour l'historique uniquement)
     */
    public void recalculateStandings(UUID completedRaceId) {
        log.info("Recalculating standings for race {}", completedRaceId);

        // 1. Récupérer tous les membres
        List<Member> members = memberRepository.findAll();

        // 2. Pour chaque membre, calculer le classement
        List<Standing> standings = new ArrayList<>();
        for (Member member : members) {
            standings.add(computeMemberStats(member));
        }

        // 3. Trier par points et assigner les rangs
        standings.sort((a, b) -> b.getTotalPoints().compareTo(a.getTotalPoints()));

        for (int i = 0; i < standings.size(); i++) {
            standings.get(i).setCurrentRank(i + 1);
        }

        // 4. Sauvegarder
        standingRepository.saveAll(standings);

        // 5. Sauvegarder dans l'historique
        saveStandingsHistory(completedRaceId, standings);

        log.info("Standings recalculated successfully");
    }

    /**
     * Sauvegarde un snapshot du classement (historique) après une course.
     *
     * @param completedRaceId l'identifiant de la course complétée
     * @param standings       la liste des classements actuels avec les rangs
     *                        assignés
     */
    private void saveStandingsHistory(UUID completedRaceId, List<Standing> standings) {
        Race race = raceRepository.findById(completedRaceId).orElseThrow();
        List<StandingsHistory> historicRecords = new ArrayList<>();

        for (Standing standing : standings) {
            StandingsHistory history = buildStandingsHistoryEntry(standing, race, completedRaceId);
            historicRecords.add(history);
        }

        standingsHistoryRepository.saveAll(historicRecords);
    }

    /**
     * Crée une entrée d'historique pour un membre après une course.
     *
     * @param standing        le classement actuel du membre
     * @param race            la course complétée
     * @param completedRaceId l'identifiant de la course
     * @return une entrée StandingsHistory prête à être sauvegardée
     */
    private StandingsHistory buildStandingsHistoryEntry(Standing standing, Race race, UUID completedRaceId) {
        StandingsHistory history = new StandingsHistory();
        history.setRace(race);
        history.setMember(standing.getMember());
        history.setRank(standing.getCurrentRank());
        history.setTotalPoints(standing.getTotalPoints());

        // Calculer les points gagnés pour cette course spécifique
        Integer racePoints = raceResultRepository
                .sumPointsByMemberAndRace(standing.getMember().getId(), completedRaceId);
        history.setPointsGained(racePoints);

        // Calculer le changement de rang par rapport à la course précédente
        Integer rankChange = calculateRankChange(standing.getMember().getId(), standing.getCurrentRank(),
                completedRaceId);
        history.setRankChange(rankChange);

        return history;
    }

    /**
     * Calcule le changement de rang d'un membre pour une course.
     * Comparaison du rang actuel avec le rang de la course précédente.
     *
     * @param memberId        l'identifiant du membre
     * @param currentRank     le rang actuel (après cette course)
     * @param completedRaceId l'identifiant de la course complétée
     * @return le changement de rang (positif = montée, négatif = descente, 0 =
     *         inchangé)
     */
    private Integer calculateRankChange(UUID memberId, Integer currentRank, UUID completedRaceId) {
        // Chercher le rang du membre à la course précédente
        Integer previousRank = standingsHistoryRepository
                .findPreviousRankByMemberAndRace(memberId, completedRaceId)
                .orElse(0);

        // Le changement est : ancien rang - nouveau rang
        // Ex: passé de 5 à 3 = +2 (montée de 2 places)
        return previousRank - currentRank;
    }

    /**
     * Calcule les stats d'un membre (points totaux, races, moyenne).
     * Agrège tous ses points gagnés, compte les courses et calcule la moyenne.
     *
     * @param member le membre pour lequel calculer les stats
     * @return un objet Standing avec points totaux, nombre de courses et moyenne
     */
    private Standing computeMemberStats(Member member) {
        // Somme des points de toutes ses courses
        Integer totalPoints = raceResultRepository.sumPointsByMember(member.getId());

        // Nombre de courses où il a participé
        Integer racesCompleted = raceResultRepository.countCompletedRacesByMember(member.getId());

        // Récupérer ou créer son Standing global
        Standing standing = standingRepository
                .findByMemberId(member.getId())
                .orElse(new Standing());

        // Remplir les informations
        standing.setMember(member);
        standing.setTotalPoints(totalPoints != null ? totalPoints : 0);
        standing.setRacesCompleted(racesCompleted != null ? racesCompleted : 0);
        standing.setAveragePointsPerRace(
                standing.getRacesCompleted() > 0
                        ? BigDecimal.valueOf(standing.getTotalPoints())
                                .divide(BigDecimal.valueOf(standing.getRacesCompleted()), 2,
                                        RoundingMode.HALF_UP)
                        : BigDecimal.ZERO);

        // Points gagnés à la dernière course
        Integer lastRacePoints = raceResultRepository.sumPointsByMemberForLastRace(member.getId());
        standing.setLastRacePoints(lastRacePoints != null ? lastRacePoints : 0);

        return standing;
    }

    /**
     * Récupérer tous les standings triés par rank
     */
    public List<Standing> getAllStandingsSorted() {
        log.debug("Fetching all standings sorted by currentRank");
        return standingRepository.findAllByOrderByCurrentRankAsc();
    }

    /**
     * Récupérer standing d'un membre
     */
    public java.util.Optional<Standing> getStandingByMemberId(UUID memberId) {
        log.debug("Fetching standing for member {}", memberId);
        return standingRepository.findByMemberId(memberId);
    }
}
