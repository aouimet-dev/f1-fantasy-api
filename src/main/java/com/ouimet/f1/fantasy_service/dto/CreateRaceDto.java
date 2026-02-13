package com.ouimet.f1.fantasy_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

/**
 * DTO pour créer une nouvelle course (admin only)
 * POST /api/admin/races
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRaceDto {

    @NotNull
    @Min(1)
    @JsonProperty("raceNumber")
    private Integer raceNumber;

    @NotBlank
    private String raceName;

    @NotBlank
    private String circuitName;

    @NotBlank
    private String country;

    @NotNull
    private LocalDate raceDate;
}
