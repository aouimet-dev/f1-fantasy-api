package com.ouimet.f1.fantasy_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO pour performance d'une équipe
 * Utilisé dans endpoints team details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamPerformanceDto {

    @NotNull
    private UUID teamId;

    @NotNull
    private String teamName;

    @NotNull
    private Integer totalPoints;

    @NotNull
    private Integer racesParticipated;

    @NotNull
    private BigDecimal averagePointsPerRace;

    private Integer bestRacePoints;

    private Integer worstRacePoints;

    /**
     * Indice de consistance (0-100)
     * Basé sur ratio: min(points) / max(points) si races >= 2
     */
    private BigDecimal consistencyScore;
}
