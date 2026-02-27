package com.ouimet.f1.fantasy_service.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ouimet.f1.fantasy_service.dto.CreateMemberTeamDto;
import com.ouimet.f1.fantasy_service.entity.Member;
import com.ouimet.f1.fantasy_service.entity.MemberTeam;
import com.ouimet.f1.fantasy_service.repository.MemberRepository;
import com.ouimet.f1.fantasy_service.repository.MemberTeamRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberTeamService {

    private final MemberTeamRepository memberTeamRepository;
    private final MemberRepository memberRepository;

    /**
     * Créer une nouvelle équipe pour un membre
     */
    public MemberTeam createMemberTeam(UUID memberId, CreateMemberTeamDto dto) {
        log.info("Creating team '{}' for member: {}", dto.getTeamName(), memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

        MemberTeam memberTeam = new MemberTeam();
        memberTeam.setMember(member);
        memberTeam.setTeamName(dto.getTeamName());
        memberTeam.setTeamOrder(dto.getTeamOrder());

        return memberTeamRepository.save(memberTeam);
    }

    /**
     * Récupérer toutes les équipes d'un membre
     */
    public List<MemberTeam> getMemberTeams(UUID memberId) {
        log.debug("Fetching teams for member: {}", memberId);
        return memberTeamRepository.findByMemberId(memberId);
    }

    /**
     * Récupérer une équipe spécifique d'un membre
     * Vérifie que l'équipe appartient bien au membre
     */
    public java.util.Optional<MemberTeam> getTeamByMemberAndTeamId(UUID memberId, UUID teamId) {
        log.debug("Fetching team {} for member {}", teamId, memberId);

        return memberTeamRepository.findById(teamId)
                .filter(team -> team.getMember().getId().equals(memberId));
    }

}
