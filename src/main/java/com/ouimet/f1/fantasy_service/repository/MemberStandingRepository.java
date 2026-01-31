package com.ouimet.f1.fantasy_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ouimet.f1.fantasy_service.entity.MemberStanding;

@Repository
public interface MemberStandingRepository extends JpaRepository<MemberStanding, Long> {

    Optional<MemberStanding> findByMemberId(Long memberId);

}
