package com.ouimet.f1.fantasy_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ouimet.f1.fantasy_service.entity.StandingsHistory;

@Repository
public interface StandingsHistoryRepository extends JpaRepository<StandingsHistory, UUID> {

    List<StandingsHistory> findByRaceIdOrderByRank(UUID raceId);

    List<StandingsHistory> findByMemberIdOrderByRaceRaceDate(UUID memberId);

    Optional<StandingsHistory> findByRaceIdAndMemberId(UUID raceId, UUID memberId);

    /**
     * Trouve le rang du membre à la course immédiatement avant la course spécifiée.
     * Utilise la date de la course pour trouver la course précédente (plus fiable
     * que l'ID).
     *
     * @param memberId        l'identifiant du membre
     * @param completedRaceId l'identifiant de la course actuelle
     * @return le rang du membre à la course précédente, ou Optional.empty() s'il
     *         n'y a pas de course précédente
     */
    @Query(value = """
            SELECT sh.rank FROM standings_history sh
            INNER JOIN races r ON sh.race_id = r.id
            INNER JOIN races current_race ON current_race.id = :completedRaceId
            WHERE sh.member_id = :memberId
            AND r.race_date = (
                SELECT MAX(r2.race_date) FROM races r2
                WHERE r2.race_date < current_race.race_date
            )
            """, nativeQuery = true)
    Optional<Integer> findPreviousRankByMemberAndRace(
            @Param("memberId") UUID memberId,
            @Param("completedRaceId") UUID completedRaceId);
}
