package com.ouimet.f1.fantasy_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ouimet.f1.fantasy_service.entity.RaceResult;

@Repository
public interface RaceResultRepository extends JpaRepository<RaceResult, UUID> {

        List<RaceResult> findByRaceId(UUID raceId);

        List<RaceResult> findByMemberTeamId(UUID memberTeamId);

        Optional<RaceResult> findByRaceIdAndMemberTeamId(UUID raceId, UUID memberTeamId);

        // Nouvelles méthodes pour le contrôleur
        List<RaceResult> findByRaceIdOrderByPointsDesc(UUID raceId);

        @Query("SELECT rr FROM RaceResult rr WHERE rr.memberTeam.id = :teamId ORDER BY rr.race.raceNumber ASC")
        List<RaceResult> findByMemberTeamIdOrderByRaceNumberAsc(@Param("teamId") UUID teamId);

        long countByRaceId(UUID raceId);

        // Requêtes custom pour les calculs
        @Query("SELECT SUM(rr.points) FROM RaceResult rr " +
                        "JOIN rr.memberTeam mt " +
                        "WHERE mt.member.id = :memberId")
        Integer sumPointsByMember(@Param("memberId") UUID memberId);

        @Query("SELECT COUNT(DISTINCT rr.race.id) FROM RaceResult rr " +
                        "JOIN rr.memberTeam mt " +
                        "WHERE mt.member.id = :memberId AND rr.race.isCompleted = true")
        Integer countCompletedRacesByMember(@Param("memberId") UUID memberId);

        @Query("SELECT SUM(rr.points) FROM RaceResult rr " +
                        "JOIN rr.memberTeam mt " +
                        "WHERE mt.member.id = :memberId AND rr.race.id = :raceId")
        Integer sumPointsByMemberAndRace(@Param("memberId") UUID memberId,
                        @Param("raceId") UUID raceId);

        @Query("SELECT SUM(rr.points) FROM RaceResult rr " +
                        "JOIN rr.memberTeam mt " +
                        "WHERE mt.member.id = :memberId AND rr.race.raceDate = (" +
                        "  SELECT MAX(r.raceDate) FROM Race r WHERE r.isCompleted = true" +
                        ")")
        Integer sumPointsByMemberForLastRace(@Param("memberId") UUID memberId);
}
