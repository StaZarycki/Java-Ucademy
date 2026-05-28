# Ucademy

A **Spring Boot 4.0.6** REST API backend for an education management platform — course enrollment, progress tracking, grading, and certificates.

**Stack:** Java 25 · Spring Boot 4 · Spring Security · Spring Data JPA · PostgreSQL 16 · JWT (jjwt 0.12.5) · SpringDoc OpenAPI 3.0.2 · Lombok · Docker

---

## Architecture & Principles

- **Layered pattern** — `@RestController` → `@Service` → `@Repository` → JPA. Controllers handle HTTP, services hold business logic, repositories abstract persistence.
- **DTOs, never entities** — No JPA entity leaks into the API layer. Responses and requests are plain DTOs constructed manually.
- **Stateless JWT auth** — `SessionCreationPolicy.STATELESS`, CSRF/form-login/http-basic disabled. Every authenticated request carries a `Bearer` token (HS256, email as subject, role as claim).
- **Role-based access** — Two roles: `USER` (default) and `ADMIN`. Admin-only: create courses, issue certificates. Authenticated-only: enroll, progress, grades. Open: registration, login, listings.
- **Global error handling** — `@ControllerAdvice` catches `IllegalArgumentException` → `400 Bad Request` JSON.
- **DB schema auto-managed** — Hibernate `ddl-auto=update` creates/evolves tables from JPA entities.
- **Containerized** — Multi-stage Docker build + `docker-compose.yml` with PostgreSQL 16 Alpine.

---

## Quick Start

```bash
# With Docker
docker compose up --build

# Or locally (need PostgreSQL running on port 5432)
./mvnw spring-boot:run
```

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## Endpoints

### Auth

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/login` | — | Login with `email` + `password`, returns JWT token |

### Users

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/users` | — | Create a new user (body: `firstName`, `lastName`, `email`, `password`, `role`) |
| GET | `/api/users` | — | List all users |
| GET | `/api/users/{id}` | — | Get user by ID |
| GET | `/api/users/me` | Authenticated | Get current authenticated user |
| DELETE | `/api/users/{id}` | — | Delete user by ID |
| GET | `/api/users/certificates` | Authenticated | Get current user's certificates |

### Courses

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/courses` | ADMIN | Create a course (body: `courseName`) |
| GET | `/api/courses` | — | List all courses |
| POST | `/api/courses/{courseId}/enroll` | Authenticated | Enroll current user in a course |
| PATCH | `/api/courses/{courseId}/progress?percentage=` | Authenticated | Update progress percentage |
| GET | `/api/courses/{courseId}/progress` | Authenticated | Get current user's progress for a course |
| GET | `/api/courses/{courseId}/grades` | Authenticated | Get current user's grades for a course |
| POST | `/api/courses/{courseId}/grades?grade=` | Authenticated | Add a grade to a course |
| POST | `/api/courses/{courseId}/issueCertificate?userEmail=` | ADMIN | Issue a certificate to a user for a course |

---

## Database

Tables auto-created by Hibernate:

| Table | Description |
|-------|-------------|
| `users` | `id`, `first_name`, `last_name`, `email` (unique), `password`, `role` |
| `courses` | `id`, `course_name` |
| `users_courses_progress` | `id`, `user_id`, `course_id`, `progress_percentage`, `status` (`IN_PROGRESS`/`DONE`), `started_at`, `completed_at` |
| `users_courses_grades` | `id`, `user_id`, `course_id`, `grade` |
| `users_courses_certificates` | `id`, `user_id`, `course_id` |

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/db` | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `user` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | `password` | DB password |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` | DDL strategy |
