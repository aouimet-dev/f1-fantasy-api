package com.ouimet.f1.fantasy_service.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ouimet.f1.fantasy_service.dto.CreateMemberDto;
import com.ouimet.f1.fantasy_service.entity.Member;
import com.ouimet.f1.fantasy_service.entity.Team;
import com.ouimet.f1.fantasy_service.exception.MemberNotFoundException;
import com.ouimet.f1.fantasy_service.repository.MemberRepository;
import com.ouimet.f1.fantasy_service.repository.TeamRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;

    public MemberService(MemberRepository memberRepository, TeamRepository teamRepository) {
        this.memberRepository = memberRepository;
        this.teamRepository = teamRepository;
    }

    private boolean memberExists(String email) {
        return memberRepository.existsById(email);
    }

    /**
     * Creates a new member with the provided details.
     * 
     * @param createMemberDto
     * @return the created member
     * @throws IllegalArgumentException if a member with the same email already exists.
     */
    public Member createMember(CreateMemberDto createMemberDto) {
        if (memberExists(createMemberDto.email())) {
            throw new IllegalArgumentException("Member with email " + createMemberDto.email() + " already exists.");
        }

        return memberRepository.save(
                new Member(createMemberDto.name(), createMemberDto.email()));
    }

    /**
     * Creates a team for the member with the given email and team name. If the
     * member does not exist, an exception is thrown.
     * 
     * @throws MemberNotFoundException if the member with the specified email does
     *                                 not exist.
     * @param email
     * @param teamName
     * @return the updated member with the newly created team
     * @throws MemberNotFoundException if the member with the specified email does
     *                                 not exist.
     */
    public Member createTeamForMember(String email, String teamName) throws MemberNotFoundException {
        Member member = memberRepository.findById(email)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with email: " + email));

        Team team = teamRepository.save(new Team(member, teamName));
        member.setTeam(team);

        return memberRepository.save(member);
    }

    /**
     * Finds a member by their email.
     * @param email the email of the member to find
     * @return an Optional containing the member if found, or empty if not found
     */
    public Optional<Member> findById(String email) {
        return memberRepository.findById(email);
    }
}
