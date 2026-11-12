# ONLYOFFICE DocSpace app for monday

## Commands

```bash
./mvnw install                      # build all modules + tests (JDK 25 required)
./mvnw test                         # all tests
./mvnw -pl onlyoffice-monday-gateway -am test -Dtest=GatewaySmokeTest -Dsurefire.failIfNoSpecifiedTests=false   # smoke
./mvnw -pl onlyoffice-monday-tenant -am test -Dtest='BoardCommandControllerTest'                                 # one test
docker compose up -d postgres redis rabbitmq   # local infra (Postgres 5432, Redis 6379, Rabbit 5672)
./mvnw -pl onlyoffice-monday-discovery spring-boot:run     # Eureka :8761 (local only)
./mvnw -pl onlyoffice-monday-user spring-boot:run          # :8080
./mvnw -pl onlyoffice-monday-tenant spring-boot:run        # :7070
SERVER_ORIGIN=https://<public-tunnel> ./mvnw -pl onlyoffice-monday-gateway spring-boot:run   # :3030
cd onlyoffice-monday-gateway/src/main/frontend && npx tailwindcss --watch -i ./styles/styles.css -o ../resources/static/main.css
```

- "cannot find symbol builder()/getX()" on Lombok classes → IDE/LSP overwrote `target/classes`
  without Lombok. Fix: `./mvnw clean install`.
- The gateway only works inside monday (needs a signed `sessionToken`); locally use a public HTTPS tunnel
  registered as the feature URL of a **dev** monday app, plus that app's client secret in env.

## Structure

- `onlyoffice-monday-gateway` — UI (Thymeleaf + HTMX + Alpine + Tailwind, `src/main/resources/templates`),
  REST `/api/1.0/{rooms,settings,webhook}`, WebSocket `/notifications`, monday JWT auth, rate limit.
- `onlyoffice-monday-tenant` — tenants, board→room links, outbox → RabbitMQ, DocSpace REST calls. Schema `tenants`.
- `onlyoffice-monday-user` — per-user DocSpace credentials. Schema `users`.
- `onlyoffice-monday-common` — shared DTOs, AES-GCM `EncryptionService`, Redis pub/sub notifications.
- `onlyoffice-monday-configuration` — shared `application-*.yml` imported by every service.
- `onlyoffice-monday-notification` — DLQ → Telegram alerts. `onlyoffice-monday-discovery` — Eureka (local only).
- `k8s/`, `*/k8s/`, `skaffold.yaml` — Kubernetes manifests (profile `kubernetes`).

## Conventions

- Java 25, Spring Boot 4 / Spring Cloud 2025.1, Lombok, Jackson 3 (`tools.jackson`) for Spring;
  gateway auth filters deliberately use Jackson 2 `ObjectMapper` (custom principal deserializer).
- `fmt-maven-plugin` reformats sources on every build (google-java-format) — build before committing.
- Commits: Conventional Commits with scope (`fix(gateway): …`, `deps(templates): …`).
- New source files carry the Apache-2.0 "(c) Copyright Ascensio System SIA" header.
- Services: controller → `service/command` | `service/query` interfaces with `Basic*` impls; inter-service
  calls via Feign with fallback factories + resilience4j (retry/circuit breaker).
- UI strings go to `gateway/src/main/resources/lang/messages*.properties` (en + es). Write "monday" lowercase.
- After changing Tailwind classes, rebuild and commit `gateway/src/main/resources/static/main.css` (it is tracked).
- DB changes: new Flyway file `V1_<n>__<name>.sql` in the service's `migration/postgres`; never edit applied ones.

## Don'ts

- Never put secrets in files (monday client secret, encryption secret, DB/Rabbit/Telegram creds) — env/k8s Secret only.
- Don't change the encryption secret or `AesEncryptionService` format: stored DocSpace credentials become unreadable.
- Don't bump the DocSpace JS SDK (`pages/root.html`, pinned 1.0.1) or shorten timeouts in
  `pages/board/create.html` / `docspace.html` without manual testing against a real DocSpace.
- Don't re-enable CSRF or add server sessions: auth is the monday `sessionToken` on every request.
- Don't commit/push or create tags unless asked.
