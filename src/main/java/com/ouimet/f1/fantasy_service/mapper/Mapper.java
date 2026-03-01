package com.ouimet.f1.fantasy_service.mapper;

import org.springframework.stereotype.Component;

import com.ouimet.f1.fantasy_service.dto.MemberDto;
import com.ouimet.f1.fantasy_service.entity.Member;

@Component
public class Mapper {
    public Member toEntity(MemberDto memberDto) {
        Member member = new Member();
        member.setEmail(memberDto.email());
        member.setName(memberDto.name());
        member.setTeam(memberDto.team());
        member.setCreatedAt(memberDto.createdAt());
        member.setUpdatedAt(memberDto.updatedAt());
        return member;
    }

    public MemberDto toDto(Member member) {
        return new MemberDto(
                member.getEmail(),
                member.getName(),
                member.getTeam(),
                member.getCreatedAt(),
                member.getUpdatedAt());
    }
}
