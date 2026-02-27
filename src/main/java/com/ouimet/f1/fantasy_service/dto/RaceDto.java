package com.ouimet.f1.fantasy_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO pour une course F1
 * Retourné par GET /api/races et /api/races/{raceId}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaceDto {

    @NotNull
    @JsonProperty("raceId")
    private UUID id;

    @NotNull
    @Min(1)
    private Integer raceNumber;

    @NotBlank
    private String raceName;

    private String circuitName;

    private String country;

    private LocalDate raceDate;

    @NotNull
    private Boolean isCompleted;

    /**
     * Total nombre de résultats entrés pour cette course
     * Utile pour afficher si tous les résultats sont in
     */
    private Integer resultCount;
}
