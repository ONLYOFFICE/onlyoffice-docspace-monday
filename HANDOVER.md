# HANDOVER — ONLYOFFICE DocSpace app for monday

## 1. Integration type

**A standalone service (SaaS), not a plugin** [code]. An app is installed in monday. Its app features, Board View and Account Settings View, open our gateway in an iframe.
ONLYOFFICE hosts the backend: `server.origin = https://monday.onlyoffice.com` in `k8s/onlyoffice-configmap.yml`
[code]. Prod runs in Kubernetes [inferred: manifests, the `kubernetes` profile, Spring Cloud Kubernetes discovery].

### Services

| Module | Port | Role | In CI/k8s |
|---|---|---|---|
| `gateway` | 3030 | UI (Thymeleaf + HTMX + Alpine + Tailwind), REST `/api/1.0/*`, WebSocket `/notifications`, auth, rate limit | yes |
| `tenant` | 7070 | tenants, board→room links, outbox → RabbitMQ, DocSpace REST calls. Postgres schema `tenants` | yes |
| `user` | 8080 | users' DocSpace credentials. Postgres schema `users` | yes |
| `notification` | 3434 | reads RabbitMQ DLQs, sends alerts to Telegram (Camel) | **no** |
| `discovery` | 8761 | Eureka, local only | no (not needed in k8s) |
| `configuration` | — | shared `application-*.yml` | — |
| `common` | — | DTOs, AES-GCM encryption, Redis pub/sub, logging | — |

Infrastructure: PostgreSQL 16, RabbitMQ 3, Redis (rate limit + pub/sub), Hazelcast (tenant/user caches),
and optionally OTLP → Prometheus/Tempo/Grafana (`docker-compose.yml`, `docker/`).

`notification` is not built by CI and has no k8s manifest, so **it is most likely not deployed in prod** [inferred].
That means nobody sees messages in the DLQs (`asc.users.*.dlq`, `asc.rooms.*.dlq`, TTL 10 min).

### How it talks to ONLYOFFICE

**DocSpace** only; the Docs editors open inside DocSpace [code].
- **Frontend** — DocSpace JS SDK **1.0.1** (`storage.onlyoffice.com/docspace/static/scripts/sdk/1.0.1/api.js`):
  - `initSystem` + `login(email, passwordHash)` — auto-login of the stored user;
  - `initManager` — shows the room on the board; creates the room (`createRoom(name, 6)` = Public Room) and tags;
  - `getHashSettings` / `createHash` — the password is hashed in the browser; only the hash reaches the server.
- **Backend** — DocSpace REST API 2.0:
  - `GET /api/2.0/security/csp` (gateway, allowlist check);
  - `POST /api/2.0/authentication`, `GET /files/rooms/{id}/links?type=1` (public link request token),
    `PUT /files/rooms/{id}/share` (invite) — tenant.
- **monday:**
  - `monday-sdk-js` from a CDN, unversioned;
  - GraphQL from the frontend (board subscribers);
  - `sessionToken` (JWT HS256, key = the app's client secret);
  - `uninstall` webhook;
  - `valueCreatedForUser`.

### Supported versions

- monday is SaaS; there are no versions.
- DocSpace:
  - cloud works (the README points to cloud sign-up) [code];
  - self-hosted should work if it has public HTTPS, Developer Tools → JavaScript SDK
    (allowlist) and `/api/2.0/security/csp` [inferred];
  - the minimum DocSpace version is not recorded anywhere [unknown].
- SDK 2.0.0 was tried and rolled back (see section 5).

---

## 2. Main features

1. **A monday admin connects DocSpace.** From Account Settings View or the first board opened:
   - the admin enters the DocSpace URL and the DocSpace admin's login and password;
   - gateway checks that the DocSpace CSP allowlist contains `https://{slug}.monday.com` and `SERVER_ORIGIN`;
   - the tenant is saved, and the admin is registered as a user automatically (outbox `CREATE_USER_ON_INITIALIZATION`).
2. **User login** (Settings → gear → App Settings). Any role, including Viewer/Guest, can
   enter their own DocSpace account (`POST /api/1.0/settings/login`).
3. **Creating a room for a board** (monday admin only):
   - the frontend, under the admin's session, creates a Public Room named after the board with the tags
     `monday integration` and `monday board - {boardId}`;
   - the frontend sends `POST /api/1.0/rooms`;
   - tenant saves the link and puts two jobs in the outbox: (a) get the public link request token,
     (b) invite the board's Team Members (not guests, not view-only) who are already registered in the app,
     with the `POWER_USER` role;
   - the outbox is polled every 30 s.
4. **Working with the room in Board View:**
   - logged-in users open it through `initManager` in `manager` mode;
   - everyone else opens it in `other` mode by `requestToken` (public link, read-only);
   - `valueCreatedForUser` is called when it is shown.
5. **Unlink room.** If the room was deleted or archived and doesn't load within the timeout, the admin sees
   an Unlink button (`DELETE /api/1.0/rooms/{boardId}`) and can create a new room.
6. **Changing DocSpace** (admin, `DELETE /api/1.0/settings`). The tenant is deleted, then all the tenant's users via the outbox.
7. **Uninstall in monday** → `POST /api/1.0/webhook` (`type=uninstall`) → the tenant is deleted.
8. **Live updates.** The `RoomCreated`, `AccessKeyRefreshed` and `TenantChanged` events go through the Redis channel
   `notifications` → WebSocket → HTMX re-renders the page.
9. **i18n** en/es by the monday user's language. **Rate limit** per IP: GET 30/min, POST 3/s.

### Manual test checklist (before a release, on a test monday account + test DocSpace)

Only the server side is automated (`GatewaySmokeTest`, see section 3). The items below need a live
monday and DocSpace, so run them by hand.

- [ ] Admin: Account Settings → enter a DocSpace without the monday domain in its JS SDK allowlist → CSP error.
- [ ] Admin: add both domains to the allowlist → login succeeds, "You have successfully logged in".
- [ ] Admin: open a board → "Create room" → a Public Room named after the board with both tags appears in DocSpace.
- [ ] Within 30–60 s the board re-renders by itself and shows the room (WebSocket + access key).
- [ ] A board Team Member who logged in to the app earlier got an invite to the room (Power User).
- [ ] A member not logged in to DocSpace sees the room in view mode (public link).
- [ ] Viewer/Guest: gear → App Settings → login with their own DocSpace account works.
- [ ] Open and edit a document in the room right from the board.
- [ ] Delete/archive the room in DocSpace → Unlink appears on the board → after unlinking, a new room can be created.
- [ ] Settings → Change DocSpace → confirm → settings are reset, boards ask for setup.
- [ ] Uninstall the app → reinstalling starts from a clean setup.
- [ ] monday language = Español → UI is in Spanish.
- [ ] monday dark theme — layout is readable.
- [ ] Narrow screen → "small screen" placeholder.

---

## 3. Build

### Toolchain and commands

- **Toolchain:**
  - JDK **25** (Temurin), Maven 3.9.9 via `./mvnw`;
  - Node 20.15.1 and npm 10.7.0 are downloaded by `frontend-maven-plugin` into
    `onlyoffice-monday-gateway/src/main/frontend/node` (gitignored).
  - **No private dependencies** — Maven Central, npm, public CDNs.
- **Commands:** `./mvnw install` (≈5 min with a cold cache); for the smoke test see `CLAUDE.md`.
  Verified on 2026-11-11 on JDK 25.0.2: all tests green:
  - user — 20;
  - tenant — 26;
  - gateway smoke — 13.
- **Images — Jib**, base `eclipse-temurin:25-jre`:
  `./mvnw -pl onlyoffice-monday-gateway jib:dockerBuild -Ddocker.image.name=<repo>/monday-gateway:<tag>`.

### Non-standard bits

- `fmt-maven-plugin` runs `format` in the `validate` phase — **every build rewrites the sources**.
- `static/main.css` (Tailwind) is built with gateway and **is tracked in git** — commit it after changing classes.
- **Lombok and the IDE.** The IDE language server (jdtls/Serena) can overwrite `target/classes` without Lombok.
  Maven then fails with `cannot find symbol builder()`. Fix: `./mvnw clean install`.
- **Profiles.** Locally — Eureka (`discovery` must be running). In k8s — `SPRING_PROFILES_ACTIVE=kubernetes`.
- **Version 1.1.0 is written in several places** — the list is in `.claude/skills/release/SKILL.md`.

### Tests

- Unit and controller tests: in tenant and user.
- k6 load scripts: `onlyoffice-monday-user/src/test/k6`.
- Gateway smoke: `onlyoffice-monday-gateway/src/test/java/com/onlyoffice/gateway/smoke/GatewaySmokeTest.java`.
  No Spring context and no external services; covers:
  - monday JWT auth: no token → 401, wrong signature → 403, admin/member roles,
    token from `HX-Current-URL`, static resources skipped;
  - uninstall webhook;
  - room creation and unlink;
  - the CSP check when saving settings;
  - credential encryption.

  Checking `@Secured(ROLE_ADMIN)` with a Spring Security context is not covered.

### Repositories and CI

- The source of truth is `https://git.onlyoffice.com/ONLYOFFICE/onlyoffice-docspace-monday` (Gitea, PRs from branches into
  `develop`) [code]. GitHub has a public mirror, `ONLYOFFICE/onlyoffice-docspace-monday`: the README sends issues
  there, and GitHub Actions run there [inferred].
- `build-monday.yaml` (manual `workflow_dispatch`):
  - builds and pushes **only gateway, tenant and user**;
  - production → `docker.io/onlyoffice/monday-{gateway,tenant,user}:<tag>`;
  - test → a private registry;
  - secrets in GitHub Secrets: `DOCKER_HUB_USERNAME`, `DOCKER_HUB_ACCESS_TOKEN`, `DOCKER_TEST_REGISTRY`,
    `DOCKER_TEST_USERNAME`, `DOCKER_TEST_ACCESS_TOKEN`;
  - access: admins of the ONLYOFFICE GitHub organization [unknown who exactly].
- `licenses.yml` — dependency license check on push/PR to master/develop. Exceptions are in `.check-licenses.yml`.

### Deployment

- `k8s/`, `*/k8s/` and `skaffold.yaml` in the repo are a template and a dev environment:
  - images without a registry or tag;
  - a Secret with dev values `postgres`/`guest`;
  - empty `clientId`/`clientSecret`.
- The real prod deploy (cluster, namespace, who rolls it out, where the prod manifests are) is not described in the
  repo [unknown]. Prod secrets should live in a k8s Secret / the DevOps secret store [inferred].

---

## 4. Publishing to the monday marketplace

- **Listing:** https://monday.com/marketplace/listing/10000860/onlyoffice-docspace.
- **Dashboard:** monday Developer Center of the account that owns the app [unknown whose account it is and who
  the admin is — the colleague needs to get a collaborator role].

### How a release works

- The code runs on our server, so **most releases are just an image deploy** with no monday involvement:
  the feature URLs don't change.
- A new app version in Developer Center and a new review are needed only if any of these change:
  - app features: added or removed, URLs;
  - OAuth scopes or permissions;
  - webhooks;
  - the listing: texts, screenshots, pricing.

  A new version is created as a draft, tested on your own account (the feature URL can point to the test
  stand), then submitted for review and made live after approval.
- **Manual steps outside the code:** listing description and screenshots, privacy policy/terms/support links,
  the security and data questionnaire, test access for reviewers (a monday account + a DocSpace with
  their domain in the allowlist).

---

## 5. Implicit knowledge

- **No server sessions of our own.** Every request to gateway carries the monday `sessionToken`: as a query parameter or in
  `HX-Current-URL`/`Referer`. The JWT is verified with HS256, key = **the monday app's client secret**.
  - Changing the secret in monday requires changing the env at the same time.
  - CSRF is disabled on purpose.
  - `ROLE_ADMIN` = monday account admin.
  - Allowed without a token: `/actuator/**`, `/main.css`, `/webSocket.js`. The webhook is checked by a separate filter
    on `Authorization`.
- **The frontend creates the room** under the DocSpace admin's session; the backend only stores the `roomId`. So
  only a monday admin who is logged in to DocSpace can create a room.
- **Template timeouts were tuned by hand** (`81c0b52`, `83a6f97`):
  - `create.html`: overall 25 s, creation 18 s, tags 10 s;
  - `docspace.html`: load timeout → Unlink, because the SDK gives no clear error for a deleted
    or archived room (`a608f63`).

  Shorter timeouts cause false "Unlink" prompts and creation errors on slow DocSpaces.
- **The `onAppReadyProcessed` flags** and destroying/recreating frames are SDK workarounds: `onAppReady` fires
  several times, and repeated failed logins broke the frame (`402a153`, `c831860`). Don't simplify them.
- **DocSpace SDK is pinned to 1.0.1.** 2.0.0 was bumped on 2025-04-30 (`c220c43`) and rolled back on 05-05 while
  preparing for review (`236e2a9`). The reason isn't in the commit — probably a behavior change or
  DocSpace version incompatibility [inferred]. Upgrade only with the full manual checklist.
- **Credentials:**
  - not the password but the DocSpace password hash is stored, encrypted with AES-GCM using the `..._ENCRYPTION_SECRET` key;
  - changing the key makes all stored credentials unreadable — every tenant would have to set up again;
  - the user's decrypted hash is sent to that same user's page for the SDK login;
  - tenant uses the admin's hash for REST calls.
- **The outbox** (`tenants.monday_outbox`) is polled every 30 s. The delay before the invite and access key
  appear is not a bug.
- **WebSocket sessions** are in the instance's memory (≤ 1000). Events travel between instances through Redis pub/sub,
  so gateway scales horizontally.
- **Hazelcast:** `tcp-ip` member-list `localhost`, kubernetes join disabled — each pod keeps its own cache
  (TTL 180 s) [code]. With several tenant/user replicas, stale reads of up to 3 min are possible.
- **Rate limit by `X-Forwarded-For`:** the ingress must pass the real IP, otherwise all clients share
  one bucket.
- **Feign clients for DocSpace** are built by hand with `Feign.builder()` and an empty target, because each
  tenant has its own URL.

---

## 6. Known issues and tech debt

### P0 — security and release blockers

- **Secrets in git.** `onlyoffice-monday-configuration/src/main/resources/application-security.yml` has
  defaults for the monday OAuth `clientId`/`clientSecret` and `encryptionSecret: secret`. Treat them as
  compromised:
  - remove the defaults (`${...}` with no value);
  - reissue the client secret in Developer Center if this is the production app;
  - check that `ENCRYPTION_SECRET` in prod is not `secret`. If it is — rotate with re-encryption
    (needs a migration).
- **CI on Java 21** while the project is on 25 — building 1.1.0 through Actions will fail.
- **`build-monday.yaml`:** `repo` has `default: testing`, but the allowed options are `test`/`production`.

### P1 — bugs

- `afterMigrateError.sql` (tenant, user): `DELETE IGNORE` is MySQL syntax. On Postgres this callback
  will fail after a failed migration.
- `@Cacheable` on `tenant/client/DocSpaceClient` has no effect: the client isn't a Spring bean. Every refresh/invite
  logs in to DocSpace again.
- Rate limiter: the POST rule reads `GET_RATE_LIMITER_REFILL/PERIOD/UNIT` (copy-paste in gateway's `application*.yml`).
- `inviteUsers`: the `TenantNotFoundException` message uses roomId instead of tenantId.
- `SettingsController.saveSettings`: the CSP origin comparison is strict — `https://x.monday.com/` with a trailing slash
  won't match. A DocSpace failure → 500 instead of a clear error.
- CDN scripts without pinned versions: `alpinejs@3.x.x`, `monday-sdk-js`. An external release can break the UI.
  Pins go in `pages/root.html`.

### P2 — tech debt

- No gateway tests with a Spring Security context (`@Secured`), no tests for notification and common.
- "v2" TODOs in the code: distributed cache, CDC instead of the outbox, `X-Timeout` handling.
- `notification` isn't deployed → DLQs aren't monitored.
- Hazelcast has no clustering in k8s.
- No tags for 1.0.1/1.0.2/1.1.0; `master` is behind.

### Branches

| Branch | State |
|---|---|
| `master` | release **1.0.0** (tag `v1.0.0`, 2025-04-03). Later releases were not merged |
| `develop` | 2025-05-16: 1.0.1/1.0.2 + later fixes (CSP, websocket limit, logging). No Boot 4 |
| `refactor/migration` | **current work, 1.1.0** (up to 2026-04-30): Spring Boot 4 + Java 25 across all modules, room creation fixes (timeouts) and unlink, dependency bumps, licenses in CI, README. Pushed, not merged into `develop`. Before release: fix CI (Java 25) and go through the manual checklist |
| `fix/logging-config`, `fix/review` | merged into `develop`, can be deleted |

What from 1.0.x is actually in prod, and whether 1.1.0 is released, is [unknown]. According to the README and CHANGELOG, 1.1.0
is being prepared, but there's no release commit or tag [inferred].
