package com.ouimet.f1.fantasy_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ouimet.f1.fantasy_service.entity.MemberTeam;

@Repository
public interface MemberTeamRepository extends JpaRepository<MemberTeam, UUID> {

    List<MemberTeam> findByMemberId(UUID memberId);

    List<MemberTeam> findByMemberIdOrderByTeamOrder(UUID memberId);

    Optional<MemberTeam> findByMemberIdAndTeamOrder(UUID memberId, Integer teamOrder);

    boolean existsByMemberIdAndTeamOrder(UUID memberId, Integer teamOrder);
}