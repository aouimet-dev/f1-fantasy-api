# F1 Fantasy Service - Documentation Technique

## Vue d'ensemble

**F1 Fantasy Service** est un service backend Spring Boot qui gère un système de fantasy pool pour la Formule 1. Il permet à des membres de créer plusieurs équipes de fantasy, de participer à des courses et de tracker leurs points dans un classement global.

**Framework**: Spring Boot 4.0.1  
**Language**: Java 25  
**Database**: PostgreSQL  
**Build Tool**: Maven  

---

## Architecture & Structure

### Stack Technique
- **Spring Boot Web MVC**: REST API
- **Spring Data JPA**: ORM et access à la base de données
- **Spring Boot Actuator**: Monitoring et health checks
- **Hibernate**: ORM avec timestamps automatiques
- **PostgreSQL**: Base de données relationnelle
- **Lombok**: Réduction du boilerplate (getters, setters, constructors)
- **SLF4J**: Logging

### Structure du Projet

```
src/main/
├── java/com/ouimet/f1/fantasy_service/
│   ├── FantasyServiceApplication.java      # Point d'entrée Spring Boot
│   ├── controller/                         # REST Endpoints
│   │   ├── AdminController.java
│   │   ├── MemberController.java
│   │   └── MemberTeamController.java
│   ├── service/                            # Business Logic
│   │   ├── MemberService.java
│   │   ├── MemberTeamService.java
│   │   ├── RaceResultService.java
│   │   └── StandingsService.java
│   ├── entity/                             # JPA Entities
│   │   ├── Member.java
│   │   ├── MemberTeam.java
│   │   ├── Race.java
│   │   ├── RaceResult.java
│   │   ├── Standing.java
│   │   └── StandingsHistory.java
│   ├── repository/                         # Data Access Layer
│   │   ├── MemberRepository.java
│   │   ├── MemberTeamRepository.java
│   │   ├── RaceRepository.java
│   │   ├── RaceResultRepository.java
│   │   ├── StandingRepository.java
│   │   └── StandingsHistoryRepository.java
│   └── dto/                                # Data Transfer Objects
│       ├── CreateMemberDto.java
│       ├── CreateMemberTeamDto.java
│       └── RaceResultDto.java
└── resources/
    ├── application.yml                    # Configuration par défaut
    ├── application-dev.yml                # Configuration développement
    ├── application-prod.yml               # Configuration production
    └── data-dev.sql                       # Données initiales dev
```

---

## Entités & Modèle de Données

### 1. **Member** (Membre)
Représente un participant au fantasy pool.

**Propriétés**:
- `id`: Long (clé primaire, auto-générée)
- `name`: String (obligatoire)
- `email`: String (unique, optionnel)
- `teams`: List\<MemberTeam\> (cascade delete)
- `createdAt`: LocalDateTime (auto-généré)
- `updatedAt`: LocalDateTime (auto-généré)

**Relations**:
- OneToMany avec `MemberTeam`: Un membre peut avoir plusieurs équipes (jusqu'à 3 théoriquement)

---

### 2. **MemberTeam** (Équipe d'un membre)
Représente une équipe dans le fantasy pool appartenant à un membre.

**Propriétés**:
- `id`: Long (clé primaire)
- `member`: Member (clé étrangère, obligatoire)
- `teamName`: String (obligatoire) - Nom de l'équipe
- `teamOrder`: Integer (obligatoire) - Ordre de l'équipe (1, 2, ou 3)
- `results`: List\<RaceResult\> - Résultats des courses pour cette équipe
- `createdAt`: LocalDateTime

**Relations**:
- ManyToOne avec `Member`: Chaque équipe appartient à exactement un membre
- OneToMany avec `RaceResult`: Chaque équipe peut avoir plusieurs résultats de courses

---

### 3. **Race** (Course)
Représente une course de Formule 1.

**Propriétés**:
- `id`: Long (clé primaire)
- `raceNumber`: Integer (unique, obligatoire) - Numéro de la course dans la saison
- `raceName`: String (obligatoire) - Nom/Grand Prix
- `circuitName`: String - Nom du circuit
- `country`: String - Pays
- `raceDate`: LocalDate - Date de la course
- `isCompleted`: Boolean (défaut: false) - Si les résultats sont saisis
- `results`: List\<RaceResult\> - Résultats de cette course
- `createdAt`: LocalDateTime

**Relations**:
- OneToMany avec `RaceResult`: Une course peut avoir plusieurs résultats

---

### 4. **RaceResult** (Résultat d'une course)
Représente le résultat d'une équipe pour une course donnée.

**Propriétés**:
- `id`: Long (clé primaire)
- `race`: Race (clé étrangère, obligatoire)
- `memberTeam`: MemberTeam (clé étrangère, obligatoire)
- `points`: Integer (défaut: 0) - Points gagnés dans cette course
- `position`: Integer - Position finale (optionnel)
- `enteredAt`: LocalDateTime (auto-généré)
- `updatedAt`: LocalDateTime

**Relations**:
- ManyToOne avec `Race`
- ManyToOne avec `MemberTeam`

---

### 5. **Standing** (Classement)
Représente la position actuelle d'un membre dans le classement global.

**Propriétés**:
- `id`: Long (clé primaire)
- `member`: Member (OneToOne unique, obligatoire)
- `totalPoints`: Integer (défaut: 0)
- `racesCompleted`: Integer (défaut: 0)
- `averagePointsPerRace`: BigDecimal - Moyenne des points par course
- `currentRank`: Integer - Position actuelle
- `lastRacePoints`: Integer - Points de la dernière course
- `lastUpdated`: LocalDateTime

**Relations**:
- OneToOne avec `Member`: Un classement par membre

---

### 6. **StandingsHistory** (Historique des classements)
Archive les positions antérieures des membres pour tracking historique.

**Propriétés**:
- Similar à `Standing` mais avec timestamp d'archivage

---

## Controllers & Endpoints

### 1. **MemberController** (`/api/members`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/members` | Créer un nouveau membre |
| GET | `/api/members/{memberId}` | Récupérer un membre par ID |

**Exemples**:
```bash
# Créer un membre
POST /api/members
{
  "name": "Alice Dupont",
  "email": "alice@example.com"
}

# Récupérer un membre
GET /api/members/1
```

---

### 2. **MemberTeamController** (`/api/members/{memberId}/teams`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/members/{memberId}/teams` | Ajouter une équipe à un membre |
| GET | `/api/members/{memberId}/teams` | Lister toutes les équipes d'un membre |

**Exemples**:
```bash
# Créer une équipe pour un membre
POST /api/members/1/teams
{
  "teamName": "Red Bulls",
  "teamOrder": 1
}

# Lister les équipes
GET /api/members/1/teams
```

---

### 3. **AdminController** (`/api/admin`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/admin/races/{raceId}/results` | Entrer les résultats d'une course |

**Exemple**:
```bash
POST /api/admin/races/1/results
[
  {
    "memberTeamId": 1,
    "points": 150,
    "position": 1
  },
  {
    "memberTeamId": 2,
    "points": 120,
    "position": 2
  }
]
```

---

## Services & Logique Métier

### 1. **MemberService**
Gère les opérations liées aux membres.

**Méthodes principales**:
- `createMember(CreateMemberDto)`: Crée un nouveau membre
- `getMemberById(Long memberId)`: Récupère un membre par ID
- `memberExists(Long memberId)`: Vérifie l'existence d'un membre

**Logging**: Les opérations sont loggées (info pour créations, debug pour lectures)

---

### 2. **MemberTeamService**
Gère les équipes des membres.

**Méthodes principales** (à implémenter/compléter):
- `createMemberTeam(Long memberId, CreateMemberTeamDto)`: Ajouter une équipe
- `getMemberTeams(Long memberId)`: Lister les équipes d'un membre

**Validations attendues**:
- Vérifie que le membre existe
- Peut valider le `teamOrder` (1-3)

---

### 3. **RaceResultService**
Gère l'entrée et le calcul des résultats de courses.

**Méthodes principales**:
- `enterRaceResults(List<RaceResultDto>, Long raceId)`: Enregistrer les résultats d'une course
- Calcule et met à jour les classements

---

### 4. **StandingsService**
Calcule et met à jour les classements des membres.

**Responsabilités**:
- Agréger les points des équipes par membre
- Classer les membres
- Archiver l'historique des classements

---

## DTOs (Data Transfer Objects)

### CreateMemberDto
```java
{
  "name": String,
  "email": String
}
```

### CreateMemberTeamDto
```java
{
  "teamName": String,
  "teamOrder": Integer
}
```

### RaceResultDto
```java
{
  "memberTeamId": Long,
  "points": Integer,
  "position": Integer
}
```

---

## Repositories

Tous les repositories héritent de `JpaRepository` et offrent des opérations CRUD de base:

- **MemberRepository**: `findById()`, `save()`, `existsById()`, etc.
- **MemberTeamRepository**: Requêtes par membre
- **RaceRepository**: Gestion des courses
- **RaceResultRepository**: Résultats de courses
- **StandingRepository**: Classements actuels
- **StandingsHistoryRepository**: Historique des classements

**À implémenter**: Requêtes personnalisées si nécessaire (ex: `findByMember()`, `findByRaceId()`)

---

## Configuration

### Profiles disponibles
- **dev**: Configuration développement (database locale, logs détaillés)
- **prod**: Configuration production

### Fichiers de configuration
- `application.yml`: Configuration par défaut
- `application-dev.yml`: Spécifique développement
- `application-prod.yml`: Spécifique production
- `data-dev.sql`: Données initiales pour dev

### Variables d'environnement
- `SPRING_PROFILES_ACTIVE`: Profil actif (par défaut: `dev`)

---

## Points Clés d'Architecture

### 1. **Cascade Delete**
`MemberTeam` a une relation `CascadeType.ALL` avec `Member`, donc supprimer un membre supprime toutes ses équipes.

### 2. **Timestamps Automatiques**
Toutes les entités ont des timestamps auto-gérés:
- `@CreationTimestamp`: Défini à la création
- `@UpdateTimestamp`: Mis à jour à chaque modification

### 3. **Validation**
Les validations métier doivent être implémentées dans les services (vérification d'existence, constraints logiques).

### 4. **Logging**
Utilise SLF4J avec Lombok `@Slf4j`. Les logs de création sont au niveau `info`, les lectures au niveau `debug`.

### 5. **Statelessness des Équipes**
Un membre peut avoir jusqu'à 3 équipes (`teamOrder`: 1, 2, 3), chacune jouant indépendamment.

---

## Flux de Données Typiques

### Inscription & Création d'équipe
```
1. POST /api/members
   → MemberController.createMember()
   → MemberService.createMember()
   → MemberRepository.save()

2. POST /api/members/1/teams
   → MemberTeamController.createMemberTeam()
   → MemberTeamService.createMemberTeam()
   → MemberTeamRepository.save()
```

### Entrée des résultats
```
1. POST /api/admin/races/1/results
   → AdminController.enterRaceResults()
   → RaceResultService.enterRaceResults()
   → RaceResultRepository.save() (pour chaque résultat)
   → StandingsService.calculateStandings()
   → StandingRepository.save() (met à jour classements)
```

---

## Fonctionnalités à Implémenter/Compléter

### Priorité Haute
- [ ] Tests unitaires complets pour tous les services
- [ ] Validation des DTOs (annotations Jakarta Validation)
- [ ] Gestion des erreurs globale (GlobalExceptionHandler)
- [ ] Vérification que le membre existe avant de créer une équipe
- [ ] Logique complète de calcul des classements

### Priorité Moyenne
- [ ] Endpoints GET pour courses
- [ ] Pagination pour les listes
- [ ] Tri des classements
- [ ] Authentification/Autorisation
- [ ] CORS si fronténd séparé

### Priorité Basse
- [ ] Caching des classements (Redis?)
- [ ] Webhooks pour notifications
- [ ] Import/Export données
- [ ] Métriques Actuator avancées

---

## Commandes Utiles

```bash
# Build
mvn clean install

# Run dev
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Run prod
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# Tests
mvn test

# Docker
docker-compose up
```

---

## Notes de Développement

### Conventions de Code
- **Packages**: `com.ouimet.f1.fantasy_service.*`
- **Suffixes**: Service, Controller, Repository, Dto
- **Logging**: Via `@Slf4j` Lombok
- **Annotations**: Lombok pour réduire boilerplate

### Dépendances Clés
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

### Améliorations Suggérées
1. Ajouter une couche de validation avec `jakarta.validation`
2. Implémenter un `GlobalExceptionHandler` pour les erreurs
3. Ajouter des tests d'intégration avec `@SpringBootTest`
4. Documenter les endpoints avec Swagger/OpenAPI
5. Implémenter le versioning d'API si nécessaire

---

## Contacts & Maintenance

- **Repository URL**: f1-fantasy-pool-f1-service
- **Framework Version**: Spring Boot 4.0.1
- **Java Version**: 25
- **Last Updated**: Février 2026

---

*Ce document est généré automatiquement. Pour toute question ou mise à jour, veuillez vérifier les commentaires du code source.*
