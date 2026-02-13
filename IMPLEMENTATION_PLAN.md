# F1 Fantasy Pool - Plan d'Implémentation Détaillé

**Créé**: Février 2026  
**Version**: 1.0  
**Projet**: F1 Fantasy Service + Frontend (Vue.js)

---

## Table des Matières

1. [Vue d'Ensemble](#vue-densemble)
2. [Phase 1: Backend - Controllers](#phase-1-backend--controllers)
3. [Phase 2: Frontend - Setup Initial](#phase-2-frontend--setup-initial)
4. [Phase 3: Frontend - Composants Réutilisables](#phase-3-frontend--composants-réutilisables)
5. [Phase 4: Frontend - Pages](#phase-4-frontend--pages)
6. [Phase 5: Intégration & Tests](#phase-5-intégration--tests)

---

## Vue d'Ensemble

### Stack Technique

#### Backend (f1-fantasy-api)
- **Framework**: Spring Boot 4.0.2
- **Language**: Java 25
- **Database**: PostgreSQL
- **Build**: Maven
- **ORM**: Hibernate/JPA
- **Libs Existantes**: Lombok, Spring Data JPA

#### Frontend (f1-fantasy-frontend) - À créer
- **Framework**: Vue.js 3 (Composition API)
- **Build Tool**: Vite
- **UI Framework**: PrimeVue 4.0+
- **Styling**: Tailwind CSS 3+
- **State Management**: Pinia  
- **HTTP Client**: Axios (via composables)
- **Routing**: Vue Router 4
- **Package Manager**: npm ou pnpm

### Architecture Globale

```
f1-fantasy-pool-gateway (OAuth Google)
         ↓ (auth headers)
f1-fantasy-frontend (Vue.js)
         ↓ (API REST)
f1-fantasy-api (Spring Boot) ← PostgreSQL
```

### Conventions de Code

#### Backend (Java)
- **Packages**: `com.ouimet.f1.fantasy_service.*`
- **Suffixes**: Service, Controller, Repository, Dto, Entity
- **Annotations**: Lombok (@Data, @RequiredArgsConstructor, @Slf4j)
- **Logging**: SLF4J avec @Slf4j
- **Timestamps**: @CreationTimestamp, @UpdateTimestamp

#### Frontend (Vue.js/TypeScript)
- **Language**: JavaScript (ou optionally TypeScript)
- **Suffixes**: .vue (components), .ts (scripts), .js (utilities)
- **Props**: camelCase, avec type validation
- **Events**: emit avec custom events
- **Stores**: Pinia stores (global state)
- **Composables**: useXxxx naming convention

---

## PHASE 1: Backend - Controllers

**Objectif**: Créer 4 contrôleurs REST pour exposer standings, races, race results  
**Durée Estimée**: 2-3 jours  
**Dépendances**: Aucune dépendance externe (déjà en place)

### 1.1 Préparation - Dépendances & Validation

#### À Ajouter au pom.xml

```xml
<!-- Jakarta Validation (nécessaire pour annotations @NotNull, @NotBlank, etc.) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- JSON Processing (si besoin de sérialisation personnalisée) -->
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

**Vérification**:
```bash
mvn clean compile
```

---

### 1.2 DTOs à Créer

#### Fichier: `src/main/java/com/ouimet/f1/fantasy_service/dto/StandingDto.java`

```java
package com.ouimet.f1.fantasy_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * DTO pour un classement (standing) global
 * Retourné par GET /api/standings et endpoints associés
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandingDto {
    
    @NotNull
    @JsonProperty("memberId")
    private Long memberId;
    
    @NotNull
    private String memberName;
    
    @NotNull
    private Integer totalPoints;
    
    @NotNull
    private Integer racesCompleted;
    
    @NotNull
    private BigDecimal averagePointsPerRace;
    
    @NotNull
    private Integer currentRank;
    
    private Integer lastRacePoints;
    
    /**
     * Pourcentage de changement de points depuis dernière course
     * Ex: 5.2 = +5.2%, -3.1 = -3.1%
     */
    private BigDecimal pointsChangePercent;
    
    /**
     * Changement de rang depuis dernière course
     * Ex: 1 = monté d'1 place, -2 = descendu de 2 places, 0 = stable
     */
    private Integer rankChange;
}
```

#### Fichier: `src/main/java/com/ouimet/f1/fantasy_service/dto/RaceDto.java`

```java
package com.ouimet.f1.fantasy_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

/**
 * DTO pour une course F1
 * Retourné par GET /api/races et /api/races/{raceId}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaceDto {
    
    @NotNull
    @JsonProperty("raceId")
    private Long id;
    
    @NotNull
    @Min(1)
    private Integer raceNumber;
    
    @NotBlank
    private String raceName;
    
    private String circuitName;
    
    private String country;
    
    private LocalDate raceDate;
    
    @NotNull
    private Boolean isCompleted;
    
    /**
     * Total nombre de résultats entrés pour cette course
     * Utile pour afficher si tous les résultats sont in
     */
    private Integer resultCount;
}
```

#### Fichier: `src/main/java/com/ouimet/f1/fantasy_service/dto/RaceResultDetailDto.java`

```java
package com.ouimet.f1.fantasy_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

/**
 * DTO pour un résultat détaillé d'une équipe pour une course
 * Retourné par GET /api/races/{raceId}/results
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaceResultDetailDto {
    
    @NotNull
    private Long raceResultId;
    
    @NotNull
    private Long raceId;
    
    @NotNull
    private String raceName;
    
    @NotNull
    private Long memberTeamId;
    
    @NotNull
    private String teamName;
    
    @NotNull
    private String memberName;
    
    @NotNull
    private Integer points;
    
    /**
     * Position finale dans cette course
     * 1 = 1er, 2 = 2ème, etc. Auto-calculée par service
     */
    private Integer position;
    
    /**
     * Timestamp d'entrée des résultats
     */
    private java.time.LocalDateTime enteredAt;
}
```

#### Fichier: `src/main/java/com/ouimet/f1/fantasy_service/dto/CreateRaceDto.java`

```java
package com.ouimet.f1.fantasy_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

/**
 * DTO pour créer une nouvelle course (admin only)
 * POST /api/admin/races
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRaceDto {
    
    @NotNull
    @Min(1)
    @JsonProperty("raceNumber")
    private Integer raceNumber;
    
    @NotBlank
    private String raceName;
    
    @NotBlank
    private String circuitName;
    
    @NotBlank
    private String country;
    
    @NotNull
    private LocalDate raceDate;
}
```

#### Fichier: `src/main/java/com/ouimet/f1/fantasy_service/dto/TeamPerformanceDto.java`

```java
package com.ouimet.f1.fantasy_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO pour performance d'une équipe
 * Utilisé dans endpoints team details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamPerformanceDto {
    
    @NotNull
    private Long teamId;
    
    @NotNull
    private String teamName;
    
    @NotNull
    private Integer totalPoints;
    
    @NotNull
    private Integer racesParticipated;
    
    @NotNull
    private BigDecimal averagePointsPerRace;
    
    private Integer bestRacePoints;
    
    private Integer worstRacePoints;
    
    /**
     * Indice de consistance (0-100)
     * Basé sur ratio: min(points) / max(points) si races >= 2
     */
    private BigDecimal consistencyScore;
}
```

---

### 1.3 Controllers à Créer

#### Fichier: `src/main/java/com/ouimet/f1/fantasy_service/controller/StandingsController.java`

```java
package com.ouimet.f1.fantasy_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ouimet.f1.fantasy_service.dto.StandingDto;
import com.ouimet.f1.fantasy_service.entity.Standing;
import com.ouimet.f1.fantasy_service.service.StandingsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller pour gérer les classements
 * Endpoints: GET /api/standings
 */
@RestController
@RequestMapping("/api/standings")
@RequiredArgsConstructor
@Slf4j
public class StandingsController {
    
    private final StandingsService standingsService;
    
    /**
     * GET /api/standings
     * 
     * Récupère TOUS les standings triés par rank ASC
     * 
     * @return List<StandingDto> triée par currentRank
     * @status 200 OK - Always returns, at least empty list
     * 
     * Example Response:
     * [
     *   {
     *     "memberId": 1,
     *     "memberName": "Alice",
     *     "totalPoints": 450,
     *     "racesCompleted": 5,
     *     "averagePointsPerRace": 90.0,
     *     "currentRank": 1,
     *     "lastRacePoints": 95,
     *     "pointsChangePercent": 5.3,
     *     "rankChange": 1
     *   },
     *   ...
     * ]
     */
    @GetMapping
    public ResponseEntity<List<StandingDto>> getAllStandings() {
        log.debug("Fetching all standings");
        List<Standing> standings = standingsService.getAllStandingsSorted();
        List<StandingDto> dtos = standings.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * GET /api/standings/{memberId}
     * 
     * Récupère le standing d'un membre spécifique
     * 
     * @param memberId ID du membre
     * @return StandingDto du membre
     * @status 200 OK - Si standing existe
     * @status 404 NOT FOUND - Si aucun standing pour ce member
     */
    @GetMapping("/{memberId}")
    public ResponseEntity<StandingDto> getMemberStanding(@PathVariable Long memberId) {
        log.debug("Fetching standing for member {}", memberId);
        return standingsService.getStandingByMemberId(memberId)
                .map(standing -> ResponseEntity.ok(mapToDto(standing)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * GET /api/standings/history/{memberId}
     * 
     * Récupère l'historique des standings d'un membre
     * Chaque entrée = standing après chaque course
     * 
     * @param memberId ID du membre
     * @return List<StandingsHistoryDto> triée par race order
     */
    @GetMapping("/history/{memberId}")
    public ResponseEntity<List<?>> getMemberStandingsHistory(@PathVariable Long memberId) {
        log.debug("Fetching standings history for member {}", memberId);
        // Impl dans service: retourner list de StandingsHistory pour ce member
        // TODO: Créer StandingsHistoryDto si pas déjà en place
        return ResponseEntity.ok().build();
    }
    
    /**
     * Helper: convertir Standing entity en StandingDto
     */
    private StandingDto mapToDto(Standing standing) {
        StandingDto dto = new StandingDto();
        dto.setMemberId(standing.getMember().getId());
        dto.setMemberName(standing.getMember().getName());
        dto.setTotalPoints(standing.getTotalPoints());
        dto.setRacesCompleted(standing.getRacesCompleted());
        dto.setAveragePointsPerRace(standing.getAveragePointsPerRace());
        dto.setCurrentRank(standing.getCurrentRank());
        dto.setLastRacePoints(standing.getLastRacePoints());
        // rankChange et pointsChangePercent à calculer via service helper
        return dto;
    }
}
```

#### Fichier: `src/main/java/com/ouimet/f1/fantasy_service/controller/RaceController.java`

```java
package com.ouimet.f1.fantasy_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ouimet.f1.fantasy_service.dto.RaceDto;
import com.ouimet.f1.fantasy_service.dto.RaceResultDetailDto;
import com.ouimet.f1.fantasy_service.entity.Race;
import com.ouimet.f1.fantasy_service.entity.RaceResult;
import com.ouimet.f1.fantasy_service.service.RaceService;
import com.ouimet.f1.fantasy_service.service.RaceResultService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller pour gérer les courses
 * Endpoints: GET /api/races
 */
@RestController
@RequestMapping("/api/races")
@RequiredArgsConstructor
@Slf4j
public class RaceController {
    
    private final RaceService raceService;
    private final RaceResultService raceResultService;
    
    /**
     * GET /api/races
     * 
     * Récupère TOUTES les courses triées par raceNumber ASC
     * Indique si complétées ou non
     * 
     * @return List<RaceDto> triée par raceNumber
     * @status 200 OK - Always returns, at least empty list
     * 
     * Example Response:
     * [
     *   {
     *     "raceId": 1,
     *     "raceNumber": 1,
     *     "raceName": "Australian Grand Prix",
     *     "circuitName": "Melbourne",
     *     "country": "Australia",
     *     "raceDate": "2026-03-15",
     *     "isCompleted": true,
     *     "resultCount": 3
     *   },
     *   {
     *     "raceId": 2,
     *     "raceNumber": 2,
     *     "raceName": "Saudi Arabian Grand Prix",
     *     "circuitName": "Jeddah",
     *     "country": "Saudi Arabia",
     *     "raceDate": "2026-03-20",
     *     "isCompleted": false,
     *     "resultCount": 0
     *   },
     *   ...
     * ]
     */
    @GetMapping
    public ResponseEntity<List<RaceDto>> getAllRaces() {
        log.debug("Fetching all races");
        List<Race> races = raceService.getAllRacesSorted();
        List<RaceDto> dtos = races.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * GET /api/races?completed={boolean}
     * 
     * Récupère races filtrées par statut completion
     * 
     * @param completed true=complétées, false=à venir
     * @return List<RaceDto>
     * @status 200 OK
     */
    @GetMapping
    @RequestParam(value = "completed", required = false)
    public ResponseEntity<List<RaceDto>> getRacesByStatus(Boolean completed) {
        log.debug("Fetching races by status: completed={}", completed);
        List<Race> races = (completed == null) ? raceService.getAllRacesSorted()
                : raceService.getRacesByCompleted(completed);
        List<RaceDto> dtos = races.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * GET /api/races/{raceId}
     * 
     * Récupère détails d'une course spécifique
     * 
     * @param raceId ID de la course
     * @return RaceDto
     * @status 200 OK - Si race existe
     * @status 404 NOT FOUND - Si race pas trouvée
     */
    @GetMapping("/{raceId}")
    public ResponseEntity<RaceDto> getRaceById(@PathVariable Long raceId) {
        log.debug("Fetching race {}", raceId);
        return raceService.getRaceById(raceId)
                .map(race -> ResponseEntity.ok(mapToDto(race)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * GET /api/races/{raceId}/results
     * 
     * Récupère les résultats d'une course
     * Triés par points DESC (meilleur score en premier)
     * 
     * @param raceId ID de la course
     * @return List<RaceResultDetailDto>
     * @status 200 OK - Si race existe (même si zéro résultats)
     * @status 404 NOT FOUND - Si race pas trouvée
     */
    @GetMapping("/{raceId}/results")
    public ResponseEntity<List<RaceResultDetailDto>> getRaceResults(@PathVariable Long raceId) {
        log.debug("Fetching results for race {}", raceId);
        
        // Vérifier que la course existe
        if (raceService.getRaceById(raceId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<RaceResult> results = raceResultService.getResultsByRaceIdSorted(raceId);
        List<RaceResultDetailDto> dtos = results.stream()
                .map(this::mapResultToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * GET /api/races/{raceId}/standings
     * 
     * Récupère les standings APRÈS une course spécifique
     * Utile pour voir l'historique du classement après chaque race
     * 
     * @param raceId ID de la course
     * @return List<StandingDto> standings historiques après this race
     * @status 200 OK
     * @status 404 NOT FOUND - Si race pas trouvée
     */
    @GetMapping("/{raceId}/standings")
    public ResponseEntity<?> getRaceStandings(@PathVariable Long raceId) {
        log.debug("Fetching standings for race {}", raceId);
        // TODO: Impl dans StandingsService: récupérer standings historiques après cette race
        // via StandingsHistory avec raceId filter
        return ResponseEntity.ok().build();
    }
    
    /**
     * Helper: convertir Race entity en RaceDto
     */
    private RaceDto mapToDto(Race race) {
        RaceDto dto = new RaceDto();
        dto.setId(race.getId());
        dto.setRaceNumber(race.getRaceNumber());
        dto.setRaceName(race.getRaceName());
        dto.setCircuitName(race.getCircuitName());
        dto.setCountry(race.getCountry());
        dto.setRaceDate(race.getRaceDate());
        dto.setIsCompleted(race.getIsCompleted());
        // resultCount: compter results pour cette race
        dto.setResultCount((int) raceResultService.countResultsByRaceId(race.getId()));
        return dto;
    }
    
    /**
     * Helper: convertir RaceResult entity en RaceResultDetailDto
     */
    private RaceResultDetailDto mapResultToDto(RaceResult result) {
        RaceResultDetailDto dto = new RaceResultDetailDto();
        dto.setRaceResultId(result.getId());
        dto.setRaceId(result.getRace().getId());
        dto.setRaceName(result.getRace().getRaceName());
        dto.setMemberTeamId(result.getMemberTeam().getId());
        dto.setTeamName(result.getMemberTeam().getTeamName());
        dto.setMemberName(result.getMemberTeam().getMember().getName());
        dto.setPoints(result.getPoints());
        dto.setPosition(result.getPosition());
        dto.setEnteredAt(result.getEnteredAt());
        return dto;
    }
}
```

#### Fichier: `src/main/java/com/ouimet/f1/fantasy_service/controller/RaceResultController.java`

```java
package com.ouimet.f1.fantasy_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ouimet.f1.fantasy_service.dto.RaceResultDetailDto;
import com.ouimet.f1.fantasy_service.dto.TeamPerformanceDto;
import com.ouimet.f1.fantasy_service.entity.RaceResult;
import com.ouimet.f1.fantasy_service.service.RaceResultService;
import com.ouimet.f1.fantasy_service.service.MemberTeamService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller pour récupérer résultats détaillés
 * Endpoints: GET /api/members/{memberId}/teams/{teamId}/results
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class RaceResultController {
    
    private final RaceResultService raceResultService;
    private final MemberTeamService memberTeamService;
    
    /**
     * GET /api/members/{memberId}/teams/{teamId}/results
     * 
     * Récupère tous les résultats d'une équipe spécifique
     * Triés par race order chronologique
     * 
     * @param memberId ID du membre
     * @param teamId ID de l'équipe
     * @return List<RaceResultDetailDto> tous les résultats
     * @status 200 OK - Si équipe existe (même si zéro résultats)
     * @status 404 NOT FOUND - Si équipe pas trouvée
     * 
     * Example Response:
     * [
     *   {
     *     "raceResultId": 101,
     *     "raceId": 1,
     *     "raceName": "Australian GP",
     *     "memberTeamId": 5,
     *     "teamName": "Red Bulls",
     *     "memberName": "Alice",
     *     "points": 120,
     *     "position": 2,
     *     "enteredAt": "2026-03-16T10:30:00"
     *   },
     *   ...
     * ]
     */
    @GetMapping("/api/members/{memberId}/teams/{teamId}/results")
    public ResponseEntity<List<RaceResultDetailDto>> getTeamResults(
            @PathVariable Long memberId,
            @PathVariable Long teamId) {
        
        log.debug("Fetching results for team {} of member {}", teamId, memberId);
        
        // Vérifier que team existe et appartient à ce member
        if (memberTeamService.getTeamByMemberAndTeamId(memberId, teamId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<RaceResult> results = raceResultService.getResultsByTeamIdSorted(teamId);
        List<RaceResultDetailDto> dtos = results.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * GET /api/members/{memberId}/teams/{teamId}/performance
     * 
     * Récupère les statistiques de performance d'une équipe
     * 
     * @param memberId ID du membre
     * @param teamId ID de l'équipe
     * @return TeamPerformanceDto
     * @status 200 OK
     * @status 404 NOT FOUND
     */
    @GetMapping("/api/members/{memberId}/teams/{teamId}/performance")
    public ResponseEntity<TeamPerformanceDto> getTeamPerformance(
            @PathVariable Long memberId,
            @PathVariable Long teamId) {
        
        log.debug("Fetching performance stats for team {} of member {}", teamId, memberId);
        
        if (memberTeamService.getTeamByMemberAndTeamId(memberId, teamId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TeamPerformanceDto performance = raceResultService.calculateTeamPerformance(teamId);
        return ResponseEntity.ok(performance);
    }
    
    /**
     * Helper: convertir RaceResult entity en RaceResultDetailDto
     */
    private RaceResultDetailDto mapToDto(RaceResult result) {
        RaceResultDetailDto dto = new RaceResultDetailDto();
        dto.setRaceResultId(result.getId());
        dto.setRaceId(result.getRace().getId());
        dto.setRaceName(result.getRace().getRaceName());
        dto.setMemberTeamId(result.getMemberTeam().getId());
        dto.setTeamName(result.getMemberTeam().getTeamName());
        dto.setMemberName(result.getMemberTeam().getMember().getName());
        dto.setPoints(result.getPoints());
        dto.setPosition(result.getPosition());
        dto.setEnteredAt(result.getEnteredAt());
        return dto;
    }
}
```

#### Modifier: `src/main/java/com/ouimet/f1/fantasy_service/controller/AdminController.java`

Ajouter cette nouvelle méthode:

```java
/**
 * POST /api/admin/races
 * 
 * Créer une nouvelle course (ADMIN ONLY)
 * 
 * @param createRaceDto DTO avec raceNumber, raceName, circuitName, country, raceDate
 * @return RaceDto créée
 * @status 201 CREATED
 * @status 400 BAD REQUEST - Si données invalides ou race déjà existe
 * @throw IllegalArgumentException - Si raceNumber déjà utilisé
 */
@PostMapping("/races")
public ResponseEntity<RaceDto> createRace(@Valid @RequestBody CreateRaceDto createRaceDto) {
    log.info("Creating new race: {}", createRaceDto.getRaceName());
    
    try {
        Race savedRace = raceService.createRace(createRaceDto);
        RaceDto dto = mapToDto(savedRace);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    } catch (IllegalArgumentException e) {
        log.warn("Invalid race creation request: {}", e.getMessage());
        return ResponseEntity.badRequest().build();
    }
}

/**
 * Helper: convertir Race entity en RaceDto
 */
private RaceDto mapToDto(Race race) {
    RaceDto dto = new RaceDto();
    dto.setId(race.getId());
    dto.setRaceNumber(race.getRaceNumber());
    dto.setRaceName(race.getRaceName());
    dto.setCircuitName(race.getCircuitName());
    dto.setCountry(race.getCountry());
    dto.setRaceDate(race.getRaceDate());
    dto.setIsCompleted(race.getIsCompleted());
    return dto;
}
```

---

### 1.4 Services à Créer/Étendre

#### À Créer: `src/main/java/com/ouimet/f1/fantasy_service/service/RaceService.java`

```java
package com.ouimet.f1.fantasy_service.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ouimet.f1.fantasy_service.dto.CreateRaceDto;
import com.ouimet.f1.fantasy_service.entity.Race;
import com.ouimet.f1.fantasy_service.repository.RaceRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les courses
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RaceService {
    
    private final RaceRepository raceRepository;
    
    /**
     * Récupérer toutes les courses triées par raceNumber
     */
    public List<Race> getAllRacesSorted() {
        log.debug("Fetching all races sorted by raceNumber");
        return raceRepository.findAllByOrderByRaceNumberAsc();
    }
    
    /**
     * Récupérer courses filtrées par statut
     */
    public List<Race> getRacesByCompleted(Boolean completed) {
        log.debug("Fetching races by completed status: {}", completed);
        return raceRepository.findByIsCompletedOrderByRaceNumberAsc(completed);
    }
    
    /**
     * Récupérer une course par ID
     */
    public Optional<Race> getRaceById(Long raceId) {
        log.debug("Fetching race {}", raceId);
        return raceRepository.findById(raceId);
    }
    
    /**
     * Créer une nouvelle course
     * @throw IllegalArgumentException si raceNumber déjà existe
     */
    public Race createRace(CreateRaceDto dto) {
        log.info("Creating race: {} (number {})", dto.getRaceName(), dto.getRaceNumber());
        
        // Vérifier que raceNumber est unique
        if (raceRepository.existsByRaceNumber(dto.getRaceNumber())) {
            throw new IllegalArgumentException("Race number " + dto.getRaceNumber() + " already exists");
        }
        
        Race race = new Race();
        race.setRaceNumber(dto.getRaceNumber());
        race.setRaceName(dto.getRaceName());
        race.setCircuitName(dto.getCircuitName());
        race.setCountry(dto.getCountry());
        race.setRaceDate(dto.getRaceDate());
        race.setIsCompleted(false);
        
        return raceRepository.save(race);
    }
}
```

#### À Étendre: `RaceRepository.java`

Ajouter ces custom queries:

```java
// Dans l'interface RaceRepository
List<Race> findAllByOrderByRaceNumberAsc();

List<Race> findByIsCompletedOrderByRaceNumberAsc(Boolean isCompleted);

boolean existsByRaceNumber(Integer raceNumber);
```

#### À Étendre: `RaceResultService.java`

Ajouter ces méthodes:

```java
/**
 * Récupérer résultats pour une course, triés par points DESC
 */
public List<RaceResult> getResultsByRaceIdSorted(Long raceId) {
    log.debug("Fetching results for race {}, sorted by points", raceId);
    return raceResultRepository.findByRaceIdOrderByPointsDesc(raceId);
}

/**
 * Récupérer résultats pour une équipe, triés par race order
 */
public List<RaceResult> getResultsByTeamIdSorted(Long teamId) {
    log.debug("Fetching results for team {}, chronologically", teamId);
    return raceResultRepository.findByMemberTeamIdOrderByRaceNumberAsc(teamId);
}

/**
 * Compter résultats pour une course
 */
public long countResultsByRaceId(Long raceId) {
    return raceResultRepository.countByRaceId(raceId);
}

/**
 * Calculer performance d'une équipe
 */
public TeamPerformanceDto calculateTeamPerformance(Long teamId) {
    // Impl: agréger points, calculer moyenne, best/worst, consistency score
    // ...
}
```

#### À Étendre: `RaceResultRepository.java`

Ajouter:

```java
List<RaceResult> findByRaceIdOrderByPointsDesc(Long raceId);

List<RaceResult> findByMemberTeamIdOrderByRaceNumberAsc(Long teamId);

long countByRaceId(Long raceId);
```

#### À Créer: `src/main/java/com/ouimet/f1/fantasy_service/service/StandingsServiceImpl.java` (extension)

Ajouter:

```java
/**
 * Récupérer tous les standings triés par rank
 */
public List<Standing> getAllStandingsSorted() {
    log.debug("Fetching all standings sorted by currentRank");
    return standingRepository.findAllByOrderByCurrentRankAsc();
}

/**
 * Récupérer standing d'un membre
 */
public Optional<Standing> getStandingByMemberId(Long memberId) {
    log.debug("Fetching standing for member {}", memberId);
    return standingRepository.findByMemberId(memberId);
}
```

#### À Étendre: `StandingRepository.java`

Ajouter:

```java
List<Standing> findAllByOrderByCurrentRankAsc();
```

---

### 1.5 Tests Unitaires à Créer

#### Fichier: `src/test/java/com/ouimet/f1/fantasy_service/controller/StandingsControllerTest.java`

```java
package com.ouimet.f1.fantasy_service.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ouimet.f1.fantasy_service.service.StandingsService;

@WebMvcTest(StandingsController.class)
public class StandingsControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private StandingsService standingsService;
    
    @Test
    public void testGetAllStandings_Success() throws Exception {
        // Given: standings service returns list
        // When: GET /api/standings
        // Then: 200 OK with standings list
        
        mockMvc.perform(get("/api/standings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
    }
    
    @Test
    public void testGetMemberStanding_NotFound() throws Exception {
        // Given: standing service returns empty
        // When: GET /api/standings/999
        // Then: 404 NOT FOUND
        
        when(standingsService.getStandingByMemberId(999L))
            .thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/standings/999"))
            .andExpect(status().isNotFound());
    }
}
```

#### Fichier: `src/test/java/com/ouimet/f1/fantasy_service/controller/RaceControllerTest.java`

(Similar structure)

---

### 1.6 Vérifications Finales Phase 1

```bash
# Compilation
mvn clean compile

# Tests
mvn test

# Build
mvn clean package -DskipTests

# Vérifier les nouveaux endpoints
curl http://localhost:8080/api/standings
curl http://localhost:8080/api/races
curl http://localhost:8080/api/races/1
curl http://localhost:8080/api/races/1/results
```

---

## PHASE 2: Frontend - Setup Initial

**Objectif**: Initialiser projet Vue.js avec PrimeVue, Tailwind, Pinia, Router  
**Durée Estimée**: 1 jour  
**Dépendances**: Node.js 18+, npm ou pnpm

### 2.1 Créer Projet Vite + Vue.js 3

```bash
cd c:\Users\alexa\Documents\Github

# Créer projet Vite avec Vue 3 + TypeScript
npm create vite@latest f1-fantasy-frontend -- --template vue-ts

cd f1-fantasy-frontend

# Installer dépendances de base
npm install

# Vérifier que ça fonctionne
npm run dev
```

À cette étape:
- ✅ Vite dev server fonctionne sur http://localhost:5173
- ✅ Vue 3 fonctionne
- ✅ Structure de base créée

### 2.2 Installer & Configurer PrimeVue

```bash
npm install primevue
npm install primeicons
npm install primeflex
```

Éditer `src/main.ts`:

```typescript
import { createApp } from 'vue'
import PrimeVue from 'primevue/config'
import 'primevue/resources/themes/lara-light-blue.css'
import 'primeicons/primeicons.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(PrimeVue)
app.use(router)

app.mount('#app')
```

### 2.3 Installer & Configurer Tailwind CSS

```bash
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
```

Éditer `tailwind.config.js`:

```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
    "./node_modules/primevue/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'f1-red': '#DC0000',      // Ferrari Red
        'f1-yellow': '#FFD700',   // Race Yellow
        'f1-silver': '#C0C0C0',   // Mercedes Silver
        'f1-orange': '#FF8700',   // McLaren Orange
        'f1-blue': '#0082FA',     // Alpine/Red Bull Blue
        'f1-green': '#00D26A',    // Aston Martin Green
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
        mono: ['Courier New', 'monospace'],
      },
    },
  },
  plugins: [],
}
```

Créer `src/assets/index.css`:

```css
@tailwind base;
@tailwind components;
@tailwind utilities;

/* F1 Custom Styles */
body {
  @apply bg-gray-50 text-gray-900 antialiased;
  font-family: 'Inter', sans-serif;
}

.f1-gradient {
  @apply bg-gradient-to-r from-f1-red via-f1-yellow to-f1-orange;
}

.f1-card {
  @apply bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow duration-300;
}

.f1-button {
  @apply px-4 py-2 bg-f1-red text-white font-semibold rounded-lg hover:bg-red-700 transition-colors duration-200;
}

.f1-badge {
  @apply px-3 py-1 rounded-full inline-flex items-center text-sm font-medium;
}

.rank-up {
  @apply text-green-600;
}

.rank-down {
  @apply text-red-600;
}

.rank-stable {
  @apply text-gray-500;
}
```

Importer dans `src/main.ts`:

```typescript
import './assets/index.css'
```

### 2.4 Installer Pinia

```bash
npm install pinia
```

Éditer `src/main.ts`:

```typescript
import { createPinia } from 'pinia'

const pinia = createPinia()
app.use(pinia)
```

### 2.5 Installer Vue Router

```bash
npm install vue-router
```

Créer `src/router/index.ts`:

```typescript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('../pages/Dashboard.vue')
  },
  {
    path: '/standings',
    name: 'Leaderboard',
    component: () => import('../pages/Leaderboard.vue')
  },
  {
    path: '/races',
    name: 'RaceCalendar',
    component: () => import('../pages/RaceCalendar.vue')
  },
  {
    path: '/races/:raceId',
    name: 'RaceDetails',
    component: () => import('../pages/RaceDetails.vue')
  },
  {
    path: '/members',
    name: 'Members',
    component: () => import('../pages/Members.vue')
  },
  {
    path: '/members/:memberId',
    name: 'MemberProfile',
    component: () => import('../pages/MemberProfile.vue')
  },
  {
    path: '/members/:memberId/teams/:teamId',
    name: 'TeamDetails',
    component: () => import('../pages/TeamDetails.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../pages/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
```

Éditer `src/main.ts`:

```typescript
import router from './router'
app.use(router)
```

### 2.6 Installer Axios

```bash
npm install axios
```

Créer `src/services/api.ts`:

```typescript
import axios from 'axios'

// Configuration de base selon l'environnement
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
})

// Interceptor pour ajouter auth headers du gateway (si disponible)
api.interceptors.request.use((config) => {
  // Token viendra du gateway via cookie/header
  // À adapter selon votre setup OAuth
  return config
})

// Gestion des erreurs globales
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error.response?.status, error.message)
    return Promise.reject(error)
  }
)

export default api
```

Créer `.env.example`:

```
VITE_API_URL=http://localhost:8080/api
VITE_GATEWAY_URL=http://localhost:3000
```

Créer `.env.development`:

```
VITE_API_URL=http://localhost:8080/api
```

### 2.7 Vérifications Phase 2

```bash
# Build success
npm run build

# No type errors
npm run type-check

# Dev server runs
npm run dev
```

Résultat attendu:
- ✅ Vite compiles sans erreurs
- ✅ UI de base charge
- ✅ Tailwind + PrimeVue inclus
- ✅ Router configuré
- ✅ API client prêt

---

## PHASE 3: Frontend - Composants Réutilisables

**Objectif**: Créer 14 composants Vue 3 réutilisables  
**Durée Estimée**: 3-4 jours  
**Ordre de Dépendances**: 
1. Petit composants sans dépendances (Badge, Spinner, etc.)
2. Composants avec appels API (Table, Card)
3. Composants complexes (Charts)

### 3.1 Petits Composants (Sans Dépendances)

#### `src/components/LoadingSpinner.vue`

```vue
<template>
  <div class="flex items-center justify-center py-20">
    <div class="space-y-3 text-center">
      <div class="flex justify-center">
        <div class="animate-spin">
          <i class="pi pi-spinner text-f1-red text-4xl"></i>
        </div>
      </div>
      <p class="text-gray-500 text-sm">{{ message }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps({
  message: {
    type: String,
    default: 'Chargement...'
  }
})
</script>
```

#### `src/components/ErrorAlert.vue`

```vue
<template>
  <div v-if="show" class="bg-red-50 border-l-4 border-f1-red p-4 mb-4">
    <div class="flex items-start">
      <div class="flex-shrink-0">
        <i class="pi pi-exclamation-circle text-f1-red"></i>
      </div>
      <div class="ml-4">
        <h3 class="text-sm font-medium text-red-900">{{ title }}</h3>
        <p class="text-sm text-red-700 mt-1">{{ message }}</p>
        <button 
          v-if="dismissible" 
          @click="show = false"
          class="mt-2 text-sm text-red-700 hover:text-red-900 font-medium"
        >
          Fermer
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps({
  title: { type: String, required: true },
  message: { type: String, required: true },
  dismissible: { type: Boolean, default: true }
})

const show = ref(true)
</script>
```

#### `src/components/RankChangeIndicator.vue`

```vue
<template>
  <div class="flex items-center gap-2">
    <span class="text-lg font-bold">{{ rank }}</span>
    <div v-if="rankChange !== 0" class="flex items-center gap-1">
      <i v-if="rankChange > 0" class="pi pi-arrow-up-right text-green-600"></i>
      <i v-else class="pi pi-arrow-down-right text-red-600"></i>
      <span :class="rankChange > 0 ? 'text-green-600' : 'text-red-600'" class="text-sm font-semibold">
        {{ Math.abs(rankChange) }}
      </span>
    </div>
    <span v-else class="text-sm text-gray-500">—</span>
  </div>
</template>

<script setup lang="ts">
defineProps({
  rank: { type: Number, required: true },
  rankChange: { type: Number, default: 0 }
})
</script>
```

#### `src/components/TeamBadge.vue`

```vue
<template>
  <div :class="[
    'f1-badge',
    teamColorClass
  ]">
    <span class="truncate">{{ teamName }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  teamName: { type: String, required: true }
})

const teamColorClass = computed(() => {
  const name = props.teamName.toLowerCase()
  if (name.includes('red') || name.includes('ferrari')) return 'bg-f1-red/20 text-f1-red'
  if (name.includes('silver') || name.includes('mercedes')) return 'bg-f1-silver/30 text-gray-800'
  if (name.includes('orange') || name.includes('mclaren')) return 'bg-f1-orange/20 text-f1-orange'
  if (name.includes('blue') || name.includes('red bull')) return 'bg-f1-blue/20 text-f1-blue'
  if (name.includes('green') || name.includes('aston')) return 'bg-f1-green/20 text-f1-green'
  return 'bg-gray-200 text-gray-800'
})
</script>
```

### 3.2 Composants avec Données

#### `src/components/StandingsTable.vue`

```vue
<template>
  <div class="f1-card p-6">
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-2xl font-bold text-gray-900">Classement Global</h2>
      <button 
        @click="refresh"
        class="p-2 hover:bg-gray-100 rounded-lg"
        title="Rafraîchir"
      >
        <i class="pi pi-refresh"></i>
      </button>
    </div>

    <div v-if="loading" class="py-4">
      <LoadingSpinner />
    </div>
    
    <div v-else class="overflow-x-auto">
      <table class="w-full">
        <thead class="bg-gray-100">
          <tr>
            <th class="px-4 py-3 text-left font-semibold text-gray-900">Rang</th>
            <th class="px-4 py-3 text-left font-semibold text-gray-900">Membre</th>
            <th class="px-4 py-3 text-right font-semibold text-gray-900">Points</th>
            <th class="px-4 py-3 text-center font-semibold text-gray-900">Courses</th>
            <th class="px-4 py-3 text-right font-semibold text-gray-900">Moy/Course</th>
            <th class="px-4 py-3 text-right font-semibold text-gray-900">Derniers Pts</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="standing in standings" :key="standing.memberId" class="border-t hover:bg-gray-50 cursor-pointer" @click="goToProfile(standing.memberId)">
            <td class="px-4 py-3">
              <RankChangeIndicator :rank="standing.currentRank" :rankChange="standing.rankChange" />
            </td>
            <td class="px-4 py-3 font-medium text-f1-blue">{{ standing.memberName }}</td>
            <td class="px-4 py-3 text-right font-bold text-lg">{{ standing.totalPoints }}</td>
            <td class="px-4 py-3 text-center">{{ standing.racesCompleted }}</td>
            <td class="px-4 py-3 text-right">{{ standing.averagePointsPerRace?.toFixed(1) }}</td>
            <td class="px-4 py-3 text-right">
              <span v-if="standing.lastRacePoints" class="font-semibold">
                +{{ standing.lastRacePoints }}
              </span>
              <span v-else class="text-gray-400">—</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStandings } from '../composables/useStandings'
import RankChangeIndicator from './RankChangeIndicator.vue'
import LoadingSpinner from './LoadingSpinner.vue'

const router = useRouter()
const { standings, loading, fetchStandings } = useStandings()

const refresh = async () => {
  await fetchStandings()
}

const goToProfile = (memberId: number) => {
  router.push(`/members/${memberId}`)
}

onMounted(async () => {
  await fetchStandings()
})
</script>
```

#### `src/components/RaceCard.vue`

```vue
<template>
  <div class="f1-card p-6 cursor-pointer hover:shadow-xl transition-all" @click="navigate">
    <div class="flex items-start justify-between mb-4">
      <div>
        <div class="text-sm font-semibold text-f1-red">Race {{ race.raceNumber }}</div>
        <h3 class="text-xl font-bold text-gray-900">{{ race.raceName }}</h3>
        <p class="text-sm text-gray-600">{{ race.circuitName }}, {{ race.country }}</p>
      </div>
      <div v-if="race.isCompleted" class="px-3 py-1 bg-green-100 text-green-700 rounded-full text-xs font-semibold">
        ✓ Complétée
      </div>
      <div v-else class="px-3 py-1 bg-yellow-100 text-yellow-700 rounded-full text-xs font-semibold">
        À venir
      </div>
    </div>
    
    <div class="flex items-center justify-between text-sm">
      <div class="text-gray-600">
        <i class="pi pi-calendar mr-2"></i>
        {{ formatDate(race.raceDate) }}
      </div>
      <div v-if="race.resultCount" class="text-gray-600">
        {{ race.resultCount }} résultats
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const props = defineProps({
  race: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const navigate = () => {
  router.push(`/races/${props.race.id}`)
}

const formatDate = (date: string) => {
  return new Intl.DateTimeFormat('fr-FR').format(new Date(date))
}
</script>
```

### 3.3 Composants Complexes

#### `src/components/PointsChart.vue`

```vue
<template>
  <div class="f1-card p-6">
    <h3 class="text-lg font-bold mb-4">Progression des Points</h3>
    <div class="flex justify-center">
      <canvas ref="chartCanvas" width="300" height="150"></canvas>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const props = defineProps({
  standingsHistory: {
    type: Array,
    default: () => []
  }
})

const chartCanvas = ref<HTMLCanvasElement | null>(null)
</script>
```

---

## PHASE 4: Frontend - Pages Principales

**Objectif**: Créer 7 pages Vue.js intégrant les composants  
**Durée Estimée**: 3-4 jours  
**Structure**: Chaque page fetche via composables, affiche composants

### 4.1 Pages Principales

#### `src/pages/Dashboard.vue`

```vue
<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-4xl font-bold f1-gradient bg-clip-text text-transparent mb-8">
      F1 Fantasy Pool 2026
    </h1>
    
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
      <!-- Season Stats Cards -->
    </div>
    
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <div class="lg:col-span-2">
        <StandingsTable />
      </div>
      <div>
        <PointsChart />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import StandingsTable from '../components/StandingsTable.vue'
import PointsChart from '../components/PointsChart.vue'
</script>
```

#### `src/pages/Leaderboard.vue`

```vue
<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold mb-6">Classement Général</h1>
    
    <StandingsTable />
  </div>
</template>

<script setup lang="ts">
import StandingsTable from '../components/StandingsTable.vue'
</script>
```

#### `src/pages/RaceCalendar.vue`

```vue
<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold mb-6">Calendrier des Courses</h1>
    
    <div class="mb-4 flex gap-2">
      <button 
        @click="filterCompleted = null"
        :class="!filterCompleted && 'f1-button'"
        class="px-4 py-2 border"
      >
        Toutes
      </button>
      <button 
        @click="filterCompleted = false"
        :class="filterCompleted === false && 'f1-button'"
        class="px-4 py-2 border"
      >
        À venir
      </button>
      <button 
        @click="filterCompleted = true"
        :class="filterCompleted === true && 'f1-button'"
        class="px-4 py-2 border"
      >
        Complétées
      </button>
    </div>
    
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <RaceCard v-for="race in filteredRaces" :key="race.id" :race="race" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRaces } from '../composables/useRaces'
import RaceCard from '../components/RaceCard.vue'

const { races, fetchRaces } = useRaces()
const filterCompleted = ref<boolean | null>(null)

const filteredRaces = computed(() => {
  if (filterCompleted.value === null) return races.value
  return races.value.filter(r => r.isCompleted === filterCompleted.value)
})

onMounted(async () => {
  await fetchRaces()
})
</script>
```

---

## PHASE 5: Intégration & Tests

**Objectif**: Tester l'API, vérifier CORS, tests E2E, déploiement  
**Durée Estimée**: 2-3 jours

### 5.1 CORS Backend

Configuration Spring Boot pour CORS (recommandé):

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

### 5.2 Lancer Full Stack

Terminal 1 - Backend:
```bash
cd f1-fantasy-api
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

Terminal 2 - Frontend:
```bash
cd f1-fantasy-frontend
npm run dev
```

Vérifier:
- ✅ Backend sur http://localhost:8080
- ✅ Frontend sur http://localhost:5173
- ✅ API calls fonctionnent
- ✅ Pas d'erreurs CORS
- ✅ Données affichées correctement

---

## Checklist d'Implémentation

### Phase 1 ✓
- [ ] DTOs créés (StandingDto, RaceDto, RaceResultDetailDto, CreateRaceDto, TeamPerformanceDto)
- [ ] StandingsController implémenté
- [ ] RaceController implémenté
- [ ] RaceResultController implémenté
- [ ] AdminController étendu avec POST /api/admin/races
- [ ] RaceService créé
- [ ] Services étendus avec méthodes (RaceResultService, StandingsService)
- [ ] Repositories mis à jour avec custom queries
- [ ] Tests unitaires écrits pour contrôleurs
- [ ] mvn clean install réussit sans erreurs

### Phase 2 ✓
- [ ] Vite project créé avec npm create vite
- [ ] PrimeVue installé & configuré dans main.ts
- [ ] Tailwind CSS installé & configuré
- [ ] Pinia installé & configuré
- [ ] Vue Router créé et configuré
- [ ] Axios client créé dans services/api.ts
- [ ] Variables d'env (.env.development créé)
- [ ] npm run dev fonctionne sans erreurs
- [ ] npm run build réussit

### Phase 3 ✓
- [ ] LoadingSpinner créé
- [ ] ErrorAlert créé
- [ ] RankChangeIndicator créé
- [ ] TeamBadge créé
- [ ] StandingsTable créé avec logique
- [ ] RaceCard créé avec navigation
- [ ] PointsChart créé
- [ ] RaceResultsTable créé
- [ ] SeasonStats créé
- [ ] Navbar créé
- [ ] MemberCard créé
- [ ] TeamCard créé
- [ ] TeamPerformanceChart créé
- [ ] Footer créé

### Phase 4 ✓
- [ ] Dashboard.vue créé
- [ ] Leaderboard.vue créé
- [ ] RaceCalendar.vue créé
- [ ] RaceDetails.vue créé
- [ ] MemberProfile.vue créé
- [ ] TeamDetails.vue créé
- [ ] Members.vue créé
- [ ] NotFound.vue créé
- [ ] Composables créés (useStandings, useRaces, useMembers, useFormatting, useF1Theme)
- [ ] Pinia stores créés (memberStore, standingsStore, raceStore, uiStore)
- [ ] Router fonctionne (navigation entre pages)

### Phase 5 ✓
- [ ] CORS configuré backend
- [ ] API calls successful
- [ ] Frontend affiche données backend
- [ ] Pas d'erreurs console
- [ ] Build production: npm run build réussit
- [ ] Tests E2E (optionnel, pour plus tard)

---

## Commandes Clés

### Backend
```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
mvn test
mvn clean package
```

### Frontend
```bash
npm install
npm run dev         # Dev server
npm run build       # Production build
npm run preview     # Preview production build
npm run type-check  # TS validation (si TypeScript)
npm run test        # Tests (optionnel)
```

---

## Notes Finales

- **Ordre d'exécution recommandé**: Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5
- **Parallélisation possible**: Après Phase 1, back et front peuvent être parallélisés
- **Test à chaque phase** avant de passer à la suivante
- **Erreurs couantes backend**: 
  - Oublier @RequestParam dans RaceController
  - DTOs sans annotations @NotNull
  - Oublier les imports de classe neue
- **Erreurs couantes frontend**: 
  - Problèmes CORS (config backend)
  - Chemins API mal configurés (.env)
  - Oublier imports composants/Router dans main.ts
- **Documentation optionnel**: Swagger/OpenAPI pour endpoints (phase 6+)

**Version du Plan**: 1.0  
**Dernière mise à jour**: Février 2026  
**Statut**: Prêt pour implémentation
