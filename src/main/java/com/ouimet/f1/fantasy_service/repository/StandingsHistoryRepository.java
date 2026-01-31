package com.ouimet.f1.fantasy_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ouimet.f1.fantasy_service.entity.StandingsHistory;

@Repository
public interface StandingsHistoryRepository extends JpaRepository<StandingsHistory, Long> {

    List<StandingsHistory> findByRaceIdOrderByRank(Long raceId);

    List<StandingsHistory> findByMemberIdOrderByRaceId(Long memberId);

    Optional<StandingsHistory> findByRaceIdAndMemberId(Long raceId, Long memberId);
}
