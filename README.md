# JobFinder AI

An AI-powered job search platform that automatically discovers, scores, and tracks opportunities tailored to your profile.

> **Sprint 1** — Foundation complete. The AI agent scaffolding is in place with a mock provider. Real agent integration ships in Sprint 2.

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Frontend** | React 18, TypeScript, Vite, Tailwind CSS, TanStack Query v5 |
| **Backend** | Java 21, Spring Boot 3.3.4, Spring Security, Spring Data JPA |
| **Database** | PostgreSQL 16 with Flyway migrations |
| **Auth** | OAuth2/OIDC — Google & Microsoft SSO (no password storage) |
| **AI (Sprint 1)** | `MockJobSearchAgent` — 15 seeded realistic jobs |

---

## Getting Started

### Prerequisites

- Java 21
- Node.js 20+
- PostgreSQL 16 (or Docker)

### 1. Clone and configure

```bash
git clone <repo-url>
cd jobfinder-ai
cp .env.example .env
```

Edit `.env` and fill in your **Google** and **Microsoft** OAuth2 credentials (see [OAuth Setup](#oauth-setup) below).

### 2a. Run with Docker (recommended)

```bash
docker compose up --build
```

Open **http://localhost** — the frontend is served through Nginx, which proxies `/api/*` to the backend.

### 2b. Run locally (two terminals)

**Backend:**
```bash
cd backend
# Export env vars or set in application.yml
export GOOGLE_CLIENT_ID=...
export GOOGLE_CLIENT_SECRET=...
export MICROSOFT_CLIENT_ID=...
export MICROSOFT_CLIENT_SECRET=...
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**. The Vite dev server proxies `/api/*` to `http://localhost:8080`.

---

## OAuth Setup

### Google

1. Go to [Google Cloud Console → APIs & Credentials](https://console.cloud.google.com/apis/credentials)
2. Create an OAuth 2.0 Client ID (Web application)
3. Add authorized redirect URI:
   - Local: `http://localhost:8080/login/oauth2/code/google`
   - Docker: `http://localhost/login/oauth2/code/google`
4. Copy Client ID and Secret to `.env`

### Microsoft

1. Go to [Azure Portal → App Registrations](https://portal.azure.com/#view/Microsoft_AAD_RegisteredApps)
2. Register a new application
3. Add redirect URI (Web platform):
   - Local: `http://localhost:8080/login/oauth2/code/microsoft`
4. Create a client secret under Certificates & Secrets
5. Copy values to `.env`

---

## Project Structure

```
jobfinder-ai/
├── backend/
│   ├── src/main/java/com/jobfinder/
│   │   ├── ai/           # Agent interface + Mock implementation
│   │   ├── auth/         # OAuth2 user service & handlers
│   │   ├── common/       # Error handling, pagination
│   │   ├── config/       # Security, app properties
│   │   ├── job/          # Job entities, service, controller
│   │   ├── linkedin/     # LinkedIn OAuth integration
│   │   ├── search/       # Job search preferences
│   │   └── user/         # User entity, service, controller
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/ # Flyway V1–V5
├── frontend/
│   └── src/
│       ├── api/          # Axios API clients
│       ├── components/   # Reusable UI components
│       ├── contexts/     # Auth & Toast providers
│       ├── pages/        # Route-level page components
│       ├── test/         # Vitest test files
│       └── types/        # TypeScript interfaces
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## API Reference

The full OpenAPI spec is available at **http://localhost:8080/swagger-ui.html** while the backend is running.

### Key Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/auth/me` | Current authenticated user |
| `POST` | `/api/auth/logout` | Sign out |
| `GET` | `/api/jobs` | List jobs with filters & pagination |
| `GET` | `/api/jobs/stats` | Dashboard stats |
| `POST` | `/api/jobs/{id}/status` | Update job status |
| `POST` | `/api/jobs/{id}/save` | Save a job |
| `GET` | `/api/preferences` | Get search preferences |
| `PUT` | `/api/preferences` | Save search preferences |
| `GET` | `/api/linkedin/status` | LinkedIn connection status |
| `GET` | `/api/linkedin/connect` | Start LinkedIn OAuth flow |
| `POST` | `/api/linkedin/disconnect` | Disconnect LinkedIn |

---

## Running Tests

**Backend:**
```bash
cd backend
./mvnw test
```

**Frontend:**
```bash
cd frontend
npm test
# or with coverage:
npm run coverage
```

---

## Database Migrations

Flyway runs automatically on startup. Migrations live in `backend/src/main/resources/db/migration/`:

| Version | Description |
|---|---|
| V1 | Users table with OAuth provider fields |
| V2 | LinkedIn connections |
| V3 | Job search preferences (with collection tables) |
| V4 | Job opportunities & saved jobs |
| V5 | 15 seeded mock jobs for Sprint 1 |

---

## LinkedIn Integration

In Sprint 1, LinkedIn runs in **mock mode** (`LINKEDIN_MOCK_MODE=true`). Clicking "Connect LinkedIn" goes through a simulated OAuth flow that sets `connected=true` with a fake email — no real LinkedIn credentials are needed.

To use real LinkedIn OAuth in Sprint 2:
1. Register at [LinkedIn Developer Portal](https://www.linkedin.com/developers/)
2. Set `LINKEDIN_MOCK_MODE=false`
3. Fill `LINKEDIN_CLIENT_ID` and `LINKEDIN_CLIENT_SECRET` in `.env`

---

## Sprint 2 Roadmap

- [ ] Replace `MockJobSearchAgent` with a real AI agent (LangChain4j / Claude API)
- [ ] LinkedIn job search via official API (requires LinkedIn Partner Program)
- [ ] Email digest notifications
- [ ] Application tracking with notes and follow-up reminders
- [ ] Advanced analytics dashboard

---

## Security Notes

- **No passwords stored** — authentication is exclusively via OAuth2/OIDC
- **Cookie-based sessions** — `JSESSIONID` cookie, server-side state
- **Secrets stay on the server** — OAuth tokens, LinkedIn credentials never reach the frontend
- **CSRF disabled** — REST API with `withCredentials` cookie auth; re-enable for form-based flows
- **CORS locked** — only `http://localhost:5173` (dev) or `APP_FRONTEND_URL` (prod)
