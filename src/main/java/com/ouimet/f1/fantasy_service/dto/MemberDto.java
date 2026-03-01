package com.ouimet.f1.fantasy_service.dto;

import java.time.LocalDateTime;

import com.ouimet.f1.fantasy_service.entity.Team;

public record MemberDto(
        String email,
        String name,
        Team team,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}