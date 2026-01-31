package com.ouimet.f1.fantasy_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ouimet.f1.fantasy_service.entity.Race;

@Repository
public interface RaceRepository extends JpaRepository<Race, Long> {
    
}
