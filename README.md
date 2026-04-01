# Unified Auth Service Fullstack

Enterprise-ready fullstack sample for a **Unified Auth Service** with:

- Spring Boot 3 + Java 17 backend
- React + Vite frontend
- JWT access and refresh tokens
- RBAC with roles and permissions
- Multi-tenant isolation
- Redis-backed rate limiting, refresh token caching, and JWT blacklist
- OAuth2-style **client credentials** token endpoint for machine-to-machine access
- PostgreSQL persistence
- Docker and docker-compose setup
- Audit logging and health checks

## Project Structure

```text
unified-auth-service-fullstack/
├── backend/
├── frontend/
├── docker-compose.yml
└── README.md
```

## Features Implemented

### Backend
- User login with tenant-aware authentication
- JWT access token and refresh token flow
- Logout with access-token blacklist + refresh-token revocation
- OAuth2-like `client_credentials` token endpoint for system clients
- Spring Security with method-level authorization
- Multi-tenant isolation using `tenantId` in token + request header enforcement
- Role/permission management
- Redis rate limiting for login and token endpoints
- Audit logs for authentication and admin operations
- Seed data for demo login and clients

### Frontend
- Login screen
- Dashboard showing current user, tenant, roles, permissions, and token expiry
- Admin users page to create users and list users in current tenant
- OAuth client token test page for machine-to-machine flow
- Protected routes and token refresh handling

## Demo Credentials

After startup, the seed users are:

- **Platform Admin**
  - username: `platformadmin`
  - password: `Admin@123`
  - tenant: `PLATFORM`
- **Bank A Trade Admin**
  - username: `tradeadmin`
  - password: `Admin@123`
  - tenant: `BANK_A`
- **Bank A Operations User**
  - username: `opsuser`
  - password: `Admin@123`
  - tenant: `BANK_A`
- **Bank B Viewer**
  - username: `viewerb`
  - password: `Admin@123`
  - tenant: `BANK_B`

Seed OAuth client:

- clientId: `trade-service`
- clientSecret: `trade-secret`
- tenant: `BANK_A`
- scopes: `read write approve`

## Run with Docker

From the project root:

```bash
docker compose up --build
```

Services:
- Backend: `http://localhost:8080`
- Frontend: `http://localhost:5173`
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`

## Run Backend Locally

```bash
cd backend
mvn spring-boot:run
```

Environment variables supported:

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=authdb
DB_USERNAME=postgres
DB_PASSWORD=postgres
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=change-this-secret-key-change-this-secret-key
JWT_ACCESS_MINUTES=15
JWT_REFRESH_DAYS=7
```

## Run Frontend Locally

```bash
cd frontend
npm install
npm run dev
```

The frontend uses `VITE_API_BASE_URL`, defaulting to `http://localhost:8080`.

## Important Request Headers

Protected API requests use:

```text
Authorization: Bearer <token>
X-Tenant-ID: <tenant code>
```

## Main APIs

### Auth
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /api/auth/me`

### OAuth2-style Client Credentials
- `POST /api/oauth2/token`

### Admin
- `GET /api/users`
- `POST /api/users`
- `GET /api/roles`
- `GET /api/tenants`
- `POST /api/oauth2/clients`

## Notes

- This project is intentionally designed as a strong end-to-end reference implementation for interview, portfolio, and learning use.
- The OAuth2 part is implemented as a secure **client credentials** token issuance flow for service-to-service integration. Browser login remains username/password + JWT.
- For production, you can extend it with OpenID Connect, MFA, email verification, and API gateway integration.
