package com.ouimet.f1.fantasy_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO pour un classement (standing) global
 * Retourné par GET /api/standings et endpoints associés
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandingDto {

    @NotNull
    @JsonProperty("memberId")
    private UUID memberId;

    @NotNull
    private String memberName;

    @NotNull
    private Integer totalPoints;

    @NotNull
    private Integer racesCompleted;

    @NotNull
    private BigDecimal averagePointsPerRace;

    @NotNull
    private Integer currentRank;

    private Integer lastRacePoints;

    /**
     * Pourcentage de changement de points depuis dernière course
     * Ex: 5.2 = +5.2%, -3.1 = -3.1%
     */
    private BigDecimal pointsChangePercent;

    /**
     * Changement de rang depuis dernière course
     * Ex: 1 = monté d'1 place, -2 = descendu de 2 places, 0 = stable
     */
    private Integer rankChange;
}
