package com.ouimet.f1.fantasy_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ouimet.f1.fantasy_service.dto.CreateMemberDto;
import com.ouimet.f1.fantasy_service.entity.Member;
import com.ouimet.f1.fantasy_service.exception.MemberNotFoundException;
import com.ouimet.f1.fantasy_service.service.MemberService;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<Member> createMember(@RequestBody CreateMemberDto createMemberDto) {
        Member savedMember = memberService.createMember(createMemberDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMember);
    }

    @GetMapping("/{email}")
    public ResponseEntity<Member> getMember(@PathVariable String email) {
        return memberService.findById(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/team")
    public ResponseEntity<Member> createTeamForMember(@RequestParam String email, @RequestParam String teamName)
            throws MemberNotFoundException {
        Member updatedMember = memberService.createTeamForMember(email, teamName);
        return ResponseEntity.ok(updatedMember);
    }

}
