package com.ouimet.f1.fantasy_service.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class RaceResultDto {
    private UUID memberTeamId;
    private Integer points;
}
