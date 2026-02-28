package com.ouimet.f1.fantasy_service.dto;

import java.time.LocalDate;

public record RaceDto(
        String raceName,
        String circuitName,
        String country,
        LocalDate raceDate) {
}
