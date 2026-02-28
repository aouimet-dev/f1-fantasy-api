package com.ouimet.f1.fantasy_service.dto;

import java.util.List;

public record CreateSeasonRacesDto(
        List<RaceDto> races) {
}
