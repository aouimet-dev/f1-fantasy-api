package com.ouimet.f1.fantasy_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ouimet.f1.fantasy_service.entity.RaceResult;

@Repository
public interface RaceResultRepository extends JpaRepository<RaceResult, Long> {

        List<RaceResult> findByRaceId(Long raceId);

        List<RaceResult> findByMemberTeamId(Long memberTeamId);

        Optional<RaceResult> findByRaceIdAndMemberTeamId(Long raceId, Long memberTeamId);

        // Nouvelles méthodes pour le contrôleur
        List<RaceResult> findByRaceIdOrderByPointsDesc(Long raceId);

        @Query("SELECT rr FROM RaceResult rr WHERE rr.memberTeam.id = :teamId ORDER BY rr.race.raceNumber ASC")
        List<RaceResult> findByMemberTeamIdOrderByRaceNumberAsc(@Param("teamId") Long teamId);

        long countByRaceId(Long raceId);

        // Requêtes custom pour les calculs
        @Query("SELECT SUM(rr.points) FROM RaceResult rr " +
                        "JOIN rr.memberTeam mt " +
                        "WHERE mt.member.id = :memberId")
        Integer sumPointsByMember(@Param("memberId") Long memberId);

        @Query("SELECT COUNT(DISTINCT rr.race.id) FROM RaceResult rr " +
                        "JOIN rr.memberTeam mt " +
                        "WHERE mt.member.id = :memberId AND rr.race.isCompleted = true")
        Integer countCompletedRacesByMember(@Param("memberId") Long memberId);

        @Query("SELECT SUM(rr.points) FROM RaceResult rr " +
                        "JOIN rr.memberTeam mt " +
                        "WHERE mt.member.id = :memberId AND rr.race.id = :raceId")
        Integer sumPointsByMemberAndRace(@Param("memberId") Long memberId,
                        @Param("raceId") Long raceId);

        @Query("SELECT SUM(rr.points) FROM RaceResult rr " +
                        "JOIN rr.memberTeam mt " +
                        "WHERE mt.member.id = :memberId AND rr.race.id = (" +
                        "  SELECT MAX(r.id) FROM Race r WHERE r.isCompleted = true" +
                        ")")
        Integer sumPointsByMemberForLastRace(@Param("memberId") Long memberId);
}
