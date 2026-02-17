package com.ouimet.f1.fantasy_service.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ouimet.f1.fantasy_service.dto.CreateMemberDto;
import com.ouimet.f1.fantasy_service.entity.Member;
import com.ouimet.f1.fantasy_service.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * Créer un nouveau membre
     */
    public Member createMember(CreateMemberDto dto) {
        log.info("Creating new member with email: {}", dto.getEmail());

        Member member = new Member();
        member.setName(dto.getName());
        member.setEmail(dto.getEmail());

        return memberRepository.save(member);
    }

    /**
     * Récupérer un membre par son ID
     */
    public Optional<Member> getMemberById(UUID memberId) {
        log.debug("Fetching member: {}", memberId);
        return memberRepository.findById(memberId);
    }

    /**
     * Vérifier si un membre existe
     */
    public boolean memberExists(UUID memberId) {
        return memberRepository.existsById(memberId);
    }

}
