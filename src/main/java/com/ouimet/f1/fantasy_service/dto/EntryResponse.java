package com.ouimet.f1.fantasy_service.dto;

import lombok.Data;

@Data
public class EntryResponse {
    private String id;
    private String entryName;
    private String playerName;
    private Integer totalPoints;
    private Integer eventPoints;
}
