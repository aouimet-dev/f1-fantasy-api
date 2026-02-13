package com.ouimet.f1.fantasy_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO pour un résultat détaillé d'une équipe pour une course
 * Retourné par GET /api/races/{raceId}/results
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaceResultDetailDto {

    @NotNull
    private Long raceResultId;

    @NotNull
    private Long raceId;

    @NotNull
    private String raceName;

    @NotNull
    private Long memberTeamId;

    @NotNull
    private String teamName;

    @NotNull
    private String memberName;

    @NotNull
    private Integer points;

    /**
     * Position finale dans cette course
     * 1 = 1er, 2 = 2ème, etc. Auto-calculée par service
     */
    private Integer position;

    /**
     * Timestamp d'entrée des résultats
     */
    private LocalDateTime enteredAt;
}
