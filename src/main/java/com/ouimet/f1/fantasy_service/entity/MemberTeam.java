package com.ouimet.f1.fantasy_service.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String teamName;

    @Column(nullable = false)
    private Integer teamOrder; // 1, 2, ou 3

    @OneToMany(mappedBy = "memberTeam")
    private List<RaceResult> results;

    @CreationTimestamp
    private LocalDateTime createdAt;
}