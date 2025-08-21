Athletics Microservices (Spring Boot, Gradle, Docker)

A polyglot storage, Spring Boot–based microservices system for managing teams, athletes, sponsors, facilities, and competitions — fronted by an API Gateway.

Tech stack: Java 17 • Spring Boot 3.4.x • Gradle 8.13 • Spring Web / WebFlux • Spring Data JPA (MySQL) • Spring Data MongoDB • Springdoc OpenAPI • Lombok • MapStruct • JaCoCo

Architecture at a glance

api-gateway — thin API layer that forwards requests to backend services and shapes responses.

team-service — teams & nested athletes (MySQL).

sponsor-service — sponsors (PostgreSQL).

facility-service — facilities (MySQL).

competition-service — competitions for a given team, also composes data from other services (MongoDB).

PlantUML C4 diagrams live in documents/ (C4L1.puml, C4L2.puml, domain.puml).

Repo layout
athletics-ms/
├─ api-gateway/
├─ team-service/
├─ sponsor-service/
├─ facility-service/
├─ competition-service/
├─ documents/            # PlantUML diagrams
├─ docker-compose.yml    # Full local stack
├─ test_all.bash         # End-to-end smoke test 
└─ gradlew, gradlew.bat, settings.gradle

Quickstart (Docker)

Prereqs: Docker Desktop (or Engine) and Docker Compose.

# From the repo root:
./gradlew clean build

# Start everything (builds images on first run)
docker-compose up --build -d

# See containers
docker compose ps
