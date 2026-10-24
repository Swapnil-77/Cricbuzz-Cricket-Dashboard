# 🏏 Cricbuzz Backend API

REST API to manage cricket **teams, players, matches and live scores**, built with Spring Boot 3, Spring Data JPA and MySQL.

## ⚙️ Tech Stack
Java 21 · Spring Boot 3.5 · Spring Data JPA (Hibernate) · MySQL 8 · Bean Validation · springdoc-openapi (Swagger UI) · JUnit 5 + MockMvc + H2 · Docker · GitHub Actions

## ✨ Highlights
- Layered architecture (controller → service → repository) with DTOs (Java records); entities are never exposed
- Match lifecycle state machine: `UPCOMING → LIVE → COMPLETED`; scores can only be updated while LIVE; result auto-computed on completion
- Global exception handling with RFC 7807 `ProblemDetail` responses (400/404/409/500)
- Pagination, sorting and filtering (`/players?teamId=1&role=BATSMAN`, `/matches?status=LIVE`)
- N+1 prevention via `@EntityGraph`; `open-in-view` disabled
- Externalised config via env vars, Dockerised, CI pipeline, integration tests

## 🔗 API (base path `/api/v1`)
| Method | Endpoint | Description |
|---|---|---|
| POST / GET | `/teams` | Create / list teams |
| GET / PUT / DELETE | `/teams/{id}` | Get / update / delete team |
| POST / GET | `/players` | Create / list players (filters: `teamId`, `role`) |
| GET / PUT / DELETE | `/players/{id}` | Get / update / delete player |
| POST / GET | `/matches` | Schedule / list matches (filter: `status`) |
| GET / DELETE | `/matches/{id}` | Get / delete match |
| PATCH | `/matches/{id}/status` | Move match to `LIVE` or `COMPLETED` |
| PATCH | `/matches/{id}/score` | Update live score for both teams |

Swagger UI: `http://localhost:8080/swagger-ui.html` · Health: `/actuator/health`

## ▶️ Run
**Docker (easiest):**
```bash
docker compose up --build
```
**Locally:** start MySQL, then
```bash
export DB_URL="jdbc:mysql://localhost:3306/cricbuzz?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true"
export DB_USERNAME=root DB_PASSWORD=yourpassword
./mvnw spring-boot:run
```
**Tests:** `./mvnw verify`

## 📮 Example
```bash
curl -X POST localhost:8080/api/v1/teams -H 'Content-Type: application/json' \
  -d '{"name":"India","shortName":"IND","country":"India"}'

curl -X PATCH localhost:8080/api/v1/matches/1/score -H 'Content-Type: application/json' \
  -d '{"team1":{"runs":180,"wickets":5,"overs":20},"team2":{"runs":170,"wickets":8,"overs":20}}'
```
