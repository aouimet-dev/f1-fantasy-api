# F1 Fantasy API — Code Review (2026-02-28)

## Executive Summary

The main startup failure is caused by incorrect JPA annotations in the entity model. After that, the project should align controller contracts with DTO responses, add centralized exception mapping, and tighten security/config conventions.

---

## Priority 0 — Build Breakers

### 1) `Member` uses wrong `@Id` annotation (Spring Data instead of JPA)

**Problem**
- `Member` currently imports `org.springframework.data.annotation.Id`.
- Hibernate/JPA expects `jakarta.persistence.Id` for entity identifiers.
- This causes startup/test failure: _Entity has no identifier_.

**Current file**
- `src/main/java/com/ouimet/f1/fantasy_service/entity/Member.java`

**Fix snippet**
```java
// before
import org.springframework.data.annotation.Id;

// after
import jakarta.persistence.Id;
```

---

### 2) `Team.member` relationship is missing `@OneToOne`

**Problem**
- `Team.member` has `@JoinColumn` but no association annotation.
- JPA requires explicit relationship metadata (`@OneToOne`, `@ManyToOne`, etc.).

**Current file**
- `src/main/java/com/ouimet/f1/fantasy_service/entity/Team.java`

**Fix snippet**
```java
import jakarta.persistence.OneToOne;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(nullable = false)
    private String teamName;

    // getters/setters
}
```

> Why `unique = true`? Your model is one-member-to-one-team (`Member` already maps `team` as `@OneToOne`).

---

## Priority 1 — API Contract & Layering

### 3) Controllers return entities instead of DTOs

**Problem**
- `MemberController` returns `ResponseEntity<Member>` directly.
- This leaks persistence internals and can create serialization surprises.
- Your project guideline says controller responses should be DTOs.

**Current file**
- `src/main/java/com/ouimet/f1/fantasy_service/controller/MemberController.java`

**Suggested DTO**
```java
package com.ouimet.f1.fantasy_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MemberDto(
        String email,
        String name,
        UUID teamId,
        String teamName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
```

**Mapper snippet (in service or dedicated mapper class)**
```java
private MemberDto toDto(Member member) {
    Team team = member.getTeam();

    return new MemberDto(
            member.getEmail(),
            member.getName(),
            team != null ? team.getId() : null,
            team != null ? team.getTeamName() : null,
            member.getCreatedAt(),
            member.getUpdatedAt());
}
```

**Controller snippet**
```java
@PostMapping
public ResponseEntity<MemberDto> createMember(@Valid @RequestBody CreateMemberDto request) {
    MemberDto created = memberService.createMember(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}

@GetMapping("/{email}")
public ResponseEntity<MemberDto> getMember(@PathVariable String email) {
    return memberService.findByEmail(email)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}
```

---

### 4) Use runtime exceptions + centralized handler

**Problem**
- `MemberNotFoundException` extends checked `Exception`, forcing throws declarations through controller.
- There is no `@ControllerAdvice` to unify API error payloads.

**Current files**
- `src/main/java/com/ouimet/f1/fantasy_service/exception/MemberNotFoundException.java`
- `src/main/java/com/ouimet/f1/fantasy_service/controller/MemberController.java`

**Fix snippet (exception type)**
```java
package com.ouimet.f1.fantasy_service.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(String message) {
        super(message);
    }
}
```

**Global handler snippet**
```java
package com.ouimet.f1.fantasy_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MemberNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleMemberNotFound(MemberNotFoundException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        detail.setTitle("Member not found");
        return detail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setTitle("Invalid request");
        return detail;
    }
}
```

---

## Priority 2 — Security & Auth Hardening

### 5) Guard `/api/auth/me` against null principal

**Problem**
- `oauthUser` can be null if endpoint is reached unauthenticated (or config evolves).

**Current file**
- `src/main/java/com/ouimet/f1/fantasy_service/controller/AuthController.java`

**Fix snippet**
```java
@GetMapping("/me")
public ResponseEntity<AuthMeResponse> me(@AuthenticationPrincipal OAuth2User oauthUser) {
    if (oauthUser == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthMeResponse(null, null, null, null, false));
    }

    String email = oauthUser.getAttribute("email");
    String fullName = oauthUser.getAttribute("name");
    String pictureUrl = oauthUser.getAttribute("picture");

    return ResponseEntity.ok(new AuthMeResponse(
            oauthUser.getName(),
            email,
            fullName,
            pictureUrl,
            true));
}
```

---

### 6) Re-enable CSRF for session-cookie API (recommended)

**Problem**
- App uses session cookies (`JSESSIONID`) + credentialed CORS.
- Full CSRF disable is risky for write endpoints.

**Current file**
- `src/main/java/com/ouimet/f1/fantasy_service/config/SecurityConfig.java`

**Recommended baseline snippet**
```java
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

http
    .cors(Customizer.withDefaults())
    .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
    .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/", "/error", "/oauth2/**", "/login/**", "/actuator/health").permitAll()
            .requestMatchers("/api/**").authenticated()
            .anyRequest().authenticated());
```

If your frontend cannot send CSRF yet, temporarily ignore only specific endpoints instead of global disable.

---

## Priority 3 — Config Hygiene

### 7) Remove hardcoded OAuth client id from source config

**Problem**
- `client-id` is currently committed directly in `application.yml`.

**Current file**
- `src/main/resources/application.yml`

**Fix snippet**
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
```

---

### 8) Align profile strategy with your own convention

You stated a preference to keep only `application.yml` + `application-prod.yml` when possible.

**Current state**
- `application.yml` defaults to `dev` profile.
- `application-dev.yml` exists.

**Suggested action**
- Move required non-prod defaults into `application.yml`.
- Keep prod overrides in `application-prod.yml`.
- Remove `application-dev.yml` once equivalent settings are relocated.

---

## Priority 4 — Testing Reality Check

### 9) Most controller tests are placeholders

**Current files**
- `src/test/java/com/ouimet/f1/fantasy_service/controller/AdminControllerTest.java`
- `src/test/java/com/ouimet/f1/fantasy_service/controller/RaceControllerTest.java`
- `src/test/java/com/ouimet/f1/fantasy_service/controller/StandingsControllerTest.java`

**Minimum useful first tests**
- `MemberService#createMember` duplicate email returns `400` via handler.
- `AuthController#/me` returns `401` when principal missing.
- `POST /api/members` returns DTO shape (not entity internals).

---

## Spring Modulith Gap

`pom.xml` has `spring-modulith.version` but no effective modulith setup yet.

**Next step snippet (when ready)**
```xml
<dependency>
  <groupId>org.springframework.modulith</groupId>
  <artifactId>spring-modulith-starter-core</artifactId>
  <version>${spring-modulith.version}</version>
</dependency>
```

Then define package modules and verify boundaries with modulith tests.

---

## Suggested Implementation Order

1. Fix `Member` JPA `@Id` import.
2. Add `@OneToOne` mapping in `Team.member`.
3. Re-run: `./mvnw.cmd test`.
4. Refactor `MemberController` to DTO responses.
5. Add global exception handler and runtime exceptions.
6. Apply security/config hardening.

---

## Verification Commands

```bash
./mvnw.cmd clean test
./mvnw.cmd clean package
```

If you want, I can apply these fixes directly in code next (starting with Priority 0 + Priority 1) and run tests after each step.
