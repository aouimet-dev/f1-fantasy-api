package com.ouimet.f1.fantasy_service.dto;

import java.util.List;

import lombok.Data;

@Data
public class LeagueResponse {
    private String id;
    private String name;
    private List<EntryBasic> entries;
}
