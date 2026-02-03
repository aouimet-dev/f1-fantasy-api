package com.ouimet.f1.fantasy_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ouimet.f1.fantasy_service.entity.Standing;

@Repository
public interface StandingRepository extends JpaRepository<Standing, Long> {

    Optional<Standing> findByMemberId(Long memberId);

}
