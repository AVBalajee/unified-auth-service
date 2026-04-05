<h1 align="center">🔐 Unified Auth Service 🚀</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Microservice-Auth%20Service-blue?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Status-Active-success?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Security-JWT%20%7C%20OAuth2-green?style=for-the-badge" />
</p>

<p align="center">
  <em>
    Enterprise-grade <b>Authentication & Authorization System</b> built using <b>Spring Boot, JWT, OAuth2, Redis, PostgreSQL</b><br>
    Designed for <b>Security 🔐 | Scalability ⚡ | Multi-Tenant Support 🏦</b>
  </em>
</p>

---

# 🧠 System Architecture

```
Frontend → API Gateway → Auth Service → PostgreSQL
                                ↓
                              Redis
                                ↓
                           JWT Validation
```

---

# 🚀 Setup

## Start Docker
```bash
docker compose up -d
```

## Run Backend
```bash
cd backend
mvn spring-boot:run
```

## Run Frontend
```bash
cd frontend
npm install
npm run dev
```

---

# 🔑 Features

- 🔐 JWT-based Authentication  
- 🔁 Refresh Token Mechanism  
- 🧑‍🤝‍🧑 Role-Based Access Control (RBAC)  
- 🏦 Multi-Tenant Isolation  
- ⚡ Redis Caching & Rate Limiting  
- 🔗 OAuth2 Client Support  
- 📊 Audit Logging  

---

# 📡 API Endpoints

## 🟢 Login
POST /api/auth/login

### Request
```json
{
  "username": "admin",
  "password": "admin123",
  "tenantCode": "BANK_A"
}
```

### Response
```json
{
  "accessToken": "jwt_token_here",
  "refreshToken": "refresh_token_here",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

---

## 🔁 Refresh Token
POST /api/auth/refresh

### Request
```json
{
  "refreshToken": "refresh_token_here"
}
```

---

## 🔴 Logout
POST /api/auth/logout

---

## 👥 Create User
POST /api/users

### Request
```json
{
  "username": "balajee",
  "password": "Password@123",
  "role": "ADMIN",
  "tenantCode": "BANK_A"
}
```

---

## 📊 Get Users
GET /api/users

---

## 🔗 OAuth Token
POST /oauth/token

---

# 🏗 Multi-Tenant Behavior

- Each user belongs to a specific tenant  
- BANK_A cannot access BANK_B data  
- Tenant validation enforced in every request  
- TenantId embedded inside JWT  

---

# 🔁 Token Flow

```
Login → Access Token → API Access
      → Refresh Token → New Access Token
```

---

# ⚡ Redis Usage

- 🔁 Refresh token storage  
- 🚫 Token blacklist (logout)  
- ⚡ Rate limiting (login protection)  

---

# 🧪 Curl Test

```bash
curl -X POST http://localhost:8080/api/auth/login \
-H "Content-Type: application/json" \
-d '{
  "username": "admin",
  "password": "admin123",
  "tenantCode": "BANK_A"
}'
```

---

# 👨‍💻 Author

**[Balajee A V](https://avbalajee.vercel.app)**
