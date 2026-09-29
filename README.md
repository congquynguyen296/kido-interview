# Authentication & User Management Service

A complete Authentication and User Management system built with Spring Boot (Backend) and React (Frontend).

## Architecture
The system follows a classic Controller-Service-Repository architecture on the backend, using JWT for authentication.

- **Frontend**: React 18, TypeScript, Vite, TailwindCSS, React Query, Zustand.
- **Backend**: Java 21, Spring Boot 3.x, Spring Security, Spring Data JPA, JWT.
- **Database**: PostgreSQL 16 (Migration with Flyway).
- **Cache / OTP**: Redis 7.

### Token Flow
1. **Access Token (JWT)**: Short TTL, kept only in memory by the frontend (Zustand store).
2. **Refresh Token (JWT)**: Long TTL, stored in a Secure, HttpOnly cookie by the backend.
3. **Silent Refresh**: When the frontend loads, it calls `/api/v1/auth/refresh` to get a new access token using the HttpOnly cookie.
4. **Token Rotation & Reuse Detection**: Every time a refresh token is used, a new pair is issued and the old one is invalidated. If a revoked refresh token is used, all sessions for that user are terminated (Reuse Detection).

## Getting Started (Development)

The project includes a `docker-compose.dev.yml` to quickly spin up the required infrastructure (PostgreSQL, Redis, MailHog) for local development.

1. **Start Infrastructure**:
   ```bash
   docker compose -f docker-compose.dev.yml up -d
   ```
2. **Start Backend**:
   Open `/backend` in your IDE, or run:
   ```bash
   cd backend
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```
   (The application will run on `http://localhost:8080`)
3. **Start Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   (The frontend will run on `http://localhost:5173`)

## Running with Docker (Production)

You can run the entire stack (PostgreSQL, Redis, MailHog, Backend, Frontend) using Docker Compose.

1. **Configure Environment**:
   Copy `.env.example` to `.env` and adjust the values (especially secrets).
   ```bash
   cp .env.example .env
   ```
2. **Build and Start**:
   ```bash
   docker compose up --build -d
   ```
3. **Access the App**:
   - Frontend: `http://localhost` (or your configured domain)
   - Backend API: `http://localhost:8080/api/v1`
   - MailHog UI: `http://localhost:8025`

## Google OAuth2 Setup
To enable Google Login:
1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create a project and navigate to **APIs & Services > Credentials**.
3. Create an **OAuth client ID** (Web application).
4. Set the **Authorized redirect URIs** to match the backend endpoint, e.g.:
   - `http://localhost:8080/login/oauth2/code/google`
5. Copy the Client ID and Client Secret to your `.env` file (`GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`).

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Spring profile (`dev` or `prod`) | `dev` |
| `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL credentials | `authdb`, `postgres`, `postgres` |
| `REDIS_PASSWORD` | Redis password | `redis` |
| `JWT_SECRET` | Secret key for JWT signing (Base64) | `your-secret-key-64-bytes` |
| `GOOGLE_CLIENT_ID` | Google OAuth Client ID | `...` |
| `GOOGLE_CLIENT_SECRET` | Google OAuth Client Secret | `...` |
| `ADMIN_EMAIL` | Bootstrap admin email | `admin@example.com` |
| `ADMIN_PASSWORD` | Bootstrap admin password | `Admin@12345` |
| `CORS_ALLOWED_ORIGINS` | Allowed origins for CORS | `http://localhost:5173` |

## API Endpoints Summary

- `POST /api/v1/auth/login` - Login
- `POST /api/v1/auth/register` - Register
- `POST /api/v1/auth/refresh` - Refresh access token
- `POST /api/v1/auth/logout` - Logout
- `GET /api/v1/users/me` - Get current user profile
- `GET /api/v1/admin/users` - List users (Admin only)
- `POST /api/v1/admin/users/{id}/reset-password` - Reset a user's password (Admin only)

(A full interactive API documentation is available via Swagger UI at `http://localhost:8080/swagger-ui/index.html` when running in `dev` profile)
