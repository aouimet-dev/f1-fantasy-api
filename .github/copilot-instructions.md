# Project Guidelines

## Code Style
- Java 25 + Spring Boot 4 with explicit constructor injection (no Lombok patterns in main code).
- Keep controllers thin and delegate business logic to services.
- DTOs are Java records in `src/main/java/com/ouimet/f1/fantasy_service/dto` (example: `CreateMemberDto`, `RaceDto`).
- JPA entities are mutable classes with annotations and explicit getters/setters in `entity/`.
- Preserve package naming and existing layering under `com.ouimet.f1.fantasy_service`.
- Controllers should return ResponseEntity with a DTO.
- Focus on the cleanest Spring patterns and architecture. The user really wants to use Spring Modulith.
- The user knows a lot of Java 25 and Spring Boot 4, so feel free to use modern features and patterns. 

## Architecture
- Follow controller -> service -> repository flow.
- Main backend boundaries live in:
  - `controller/` for HTTP mapping and response shaping
  - `service/` for business rules
  - `repository/` for persistence
  - `entity/` for JPA models
  - `config/` for security and app wiring
- Keep auth/session concerns inside `config/SecurityConfig.java` and auth endpoints.

## Build and Test
- Use Maven wrapper from repo root:
  - `./mvnw.cmd test`
  - `./mvnw.cmd clean package`
  - `./mvnw.cmd spring-boot:run`
- Local DB dependency is in `docker-compose.yml` (Postgres service).
- Current tests are minimal, let the user write its own whenever he is ready.

## Project Conventions
- Runtime config uses layered YAML files:
  - `src/main/resources/application.yml`
  - `src/main/resources/application-dev.yml`
  - `src/main/resources/application-prod.yml`
- Default app port is configured in `application.yml`.
- If possible, aim to keep application.yml and application-prod.yml, but not application-dev.yml.
- Preserve existing endpoint roots (`/api/**`, `/oauth2/**`, `/login/**`).

## Integration Points
- Primary external dependencies are PostgreSQL and Google OAuth2 provider configuration.
- Keep API endpoint roots stable (`/api/**`, `/oauth2/**`, `/login/**`) unless explicitly changing contract.

## Security
- Security is session-cookie based with OAuth2 login (Google) and credentialed CORS.
- `SecurityConfig` currently:
  - protects `/api/**`
  - allows OAuth/login/health/public paths
  - sets custom 401 entrypoint for API requests
- When editing CORS/auth rules, keep `allowCredentials(true)` and approved origins tightly scoped.
- Avoid broadening public routes without explicit need.
- Keep security tight and only allow for the frontend app to access the API.
