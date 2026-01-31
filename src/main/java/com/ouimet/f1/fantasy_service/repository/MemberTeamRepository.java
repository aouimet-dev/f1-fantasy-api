package com.ouimet.f1.fantasy_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ouimet.f1.fantasy_service.entity.MemberTeam;

@Repository
public interface MemberTeamRepository extends JpaRepository<MemberTeam, Long> {

    List<MemberTeam> findByMemberId(Long memberId);

    List<MemberTeam> findByMemberIdOrderByTeamOrder(Long memberId);

    Optional<MemberTeam> findByMemberIdAndTeamOrder(Long memberId, Integer teamOrder);

    boolean existsByMemberIdAndTeamOrder(Long memberId, Integer teamOrder);
}