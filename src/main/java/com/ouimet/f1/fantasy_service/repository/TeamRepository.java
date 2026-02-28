package com.ouimet.f1.fantasy_service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ouimet.f1.fantasy_service.entity.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {
}