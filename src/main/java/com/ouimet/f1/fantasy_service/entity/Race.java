package com.ouimet.f1.fantasy_service.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "races")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Race {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private Integer raceNumber;

    @Column(nullable = false)
    private String raceName;

    private String circuitName;
    private String country;

    private LocalDate raceDate;

    @Column(nullable = false)
    private Boolean isCompleted = false;

    @OneToMany(mappedBy = "race")
    private List<RaceResult> results;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
