package com.ouimet.f1.fantasy_service.dto;

import java.util.UUID;

public record MemberTeamDto(
        UUID id,
        String teamName,
        Integer teamOrder) {
}
