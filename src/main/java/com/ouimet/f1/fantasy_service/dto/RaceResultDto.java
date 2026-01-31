package com.ouimet.f1.fantasy_service.dto;

import lombok.Data;

@Data
public class RaceResultDto {
    private Long memberTeamId;
    private Integer points;
    private Integer position;
}
