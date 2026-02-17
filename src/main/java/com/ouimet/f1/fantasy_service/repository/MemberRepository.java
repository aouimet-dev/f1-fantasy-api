package com.ouimet.f1.fantasy_service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ouimet.f1.fantasy_service.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {
    
}
