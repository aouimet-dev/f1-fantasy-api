package com.ouimet.f1.fantasy_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ouimet.f1.fantasy_service.entity.Standing;

@Repository
public interface StandingRepository extends JpaRepository<Standing, UUID> {

    Optional<Standing> findByMemberId(UUID memberId);

    List<Standing> findAllByOrderByCurrentRankAsc();
}
