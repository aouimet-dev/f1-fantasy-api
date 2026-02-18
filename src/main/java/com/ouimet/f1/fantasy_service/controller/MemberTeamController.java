package com.ouimet.f1.fantasy_service.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ouimet.f1.fantasy_service.dto.CreateMemberTeamDto;
import com.ouimet.f1.fantasy_service.dto.MemberTeamDto;
import com.ouimet.f1.fantasy_service.entity.MemberTeam;
import com.ouimet.f1.fantasy_service.service.MemberTeamService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members/{memberId}/teams")
@RequiredArgsConstructor
public class MemberTeamController {

    private final MemberTeamService memberTeamService;

    /**
     * Ajouter une équipe à un membre
     */
    @PostMapping
    public ResponseEntity<MemberTeamDto> createMemberTeam(
            @PathVariable UUID memberId,
            @RequestBody CreateMemberTeamDto dto) {

        MemberTeam savedTeam = memberTeamService.createMemberTeam(memberId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDto(savedTeam));
    }

    /**
     * Récupérer toutes les équipes d'un membre
     */
    @GetMapping
    public ResponseEntity<List<MemberTeamDto>> getMemberTeams(@PathVariable UUID memberId) {
        List<MemberTeam> teams = memberTeamService.getMemberTeams(memberId);
        return ResponseEntity.ok(teams.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    private MemberTeamDto mapToDto(MemberTeam memberTeam) {
        return new MemberTeamDto(
                memberTeam.getId(),
                memberTeam.getTeamName(),
                memberTeam.getTeamOrder());
    }

}
