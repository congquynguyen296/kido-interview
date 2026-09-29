# Business Logic & System Flow Documentation

> Tài liệu mô tả chi tiết các luồng nghiệp vụ và logic của hệ thống Auth Service.

---

## 1. Kiến trúc tổng quan

```
[Browser / Client]
      │
      ▼
[Frontend: React + Vite]  ←→  [Backend: Spring Boot]
                                    │
                          ┌─────────┴─────────┐
                          ▼                   ▼
                    [PostgreSQL]           [Redis]
                    (Persistent)         (Cache/OTP)
                          
[MailHog / SMTP] ← Backend gửi email xác thực, OTP, thông báo
```

### Phân tầng Backend
| Layer | Mô tả |
|-------|-------|
| **Controller** | Nhận HTTP request, log, validate, gọi Service, trả response |
| **Service** | Business logic thuần, không biết về HTTP |
| **Repository** | JPA queries, chỉ truy cập DB |
| **Mapper** | Chuyển đổi Entity ↔ DTO thủ công (không dùng MapStruct) |

---

## 2. Authentication Flows

### 2.1 Đăng ký tài khoản (Register)

```
Client                      Backend                    DB/Redis/Mail
  │                            │                            │
  │── POST /auth/register ─────►                            │
  │   {email, password,        │                            │
  │    confirmPassword,        │── Check email exists ─────►│
  │    fullName}               │◄── Not exists ─────────────│
  │                            │                            │
  │                            │── Generate username        │
  │                            │   (email prefix, unique)   │
  │                            │                            │
  │                            │── Hash password (BCrypt)   │
  │                            │── Save user ──────────────►│
  │                            │◄── Saved ─────────────────│
  │                            │                            │
  │                            │── Generate OTP token ─────►Redis (TTL)
  │                            │── Send verify email ───────►SMTP/MailHog
  │                            │                            │
  │◄── 200 RegisterResponse ───│                            │
  │    {userId, email,         │                            │
  │     emailVerificationRequired}                          │
```

**Logic chi tiết:**
- Password và confirmPassword phải khớp (validate ở backend)
- `username` tự động = phần trước `@` của email (ví dụ: `john@gmail.com` → `john`)
- Nếu `john` đã tồn tại → thử `john1`, `john2`, ...
- `emailVerified = false` khi `require-email-verification: true`
- Email xác thực **luôn được gửi** dù cờ dev/prod
- Token xác thực email được lưu vào Redis với TTL cấu hình

---

### 2.2 Xác thực Email (Verify Email)

```
Client                      Backend                    Redis / DB
  │                            │                            │
  │─ Click link trong email    │                            │
  │  /verify-email?token=xxx   │                            │
  │                            │                            │
  │── POST /auth/verify-email ─►                            │
  │   {email, token}           │── Get token from Redis ───►│
  │                            │◄── Token value ────────────│
  │                            │                            │
  │                            │── Compare tokens           │
  │                            │── Mark emailVerified=true ►DB
  │                            │── Delete token from Redis ►│
  │                            │                            │
  │◄── 200 VerifyEmailResponse ─│                            │
```

---

### 2.3 Đăng nhập (Login)

```
Client                      Backend                    DB / Redis
  │                            │                            │
  │── POST /auth/login ────────►                            │
  │   {usernameOrEmail,        │                            │
  │    password}               │── Find user by email/      │
  │                            │   username ───────────────►│
  │                            │◄── User ───────────────────│
  │                            │                            │
  │                            │── Verify password (BCrypt) │
  │                            │── Check user status        │
  │                            │── Check emailVerified      │
  │                            │                            │
  │                            │── Generate Access Token    │
  │                            │   (JWT, short TTL: 15m)    │
  │                            │── Generate Refresh Token   │
  │                            │   (JWT, long TTL: 7d)      │
  │                            │── Save Refresh Token ─────►DB (token_validation)
  │                            │── Update lastLoginAt ─────►DB
  │                            │                            │
  │◄── 200 LoginResponse ───────│                            │
  │    Header: Set-Cookie:     │                            │
  │      refresh_token=xxx;    │                            │
  │      HttpOnly; Secure      │                            │
  │    Body: {accessToken,     │                            │
  │           user}            │                            │
```

**Điều kiện login thành công:**
1. Tìm thấy user theo email hoặc username
2. BCrypt verify password khớp
3. `status = ACTIVE` (không LOCKED/DELETED/INACTIVE)
4. `emailVerified = true` (nếu `require-email-verification: true`)

---

### 2.4 Silent Refresh Token

```
Browser Load               Backend                    DB / Redis
  │                            │                            │
  │── POST /auth/refresh ──────►                            │
  │   Cookie: refresh_token=xxx│                            │
  │                            │── Extract token from cookie│
  │                            │── Validate JWT signature   │
  │                            │── Check token in DB ──────►│
  │                            │◄── Token valid ────────────│
  │                            │                            │
  │                            │── Check if token revoked   │
  │                            │   (Redis blacklist)        │
  │                            │                            │
  │                            │── Issue new Access Token   │
  │                            │── Issue new Refresh Token  │
  │                            │   (Token Rotation)         │
  │                            │── Revoke old Refresh Token►DB
  │                            │── Save new Refresh Token ─►DB
  │                            │                            │
  │◄── 200 RefreshTokenResponse─│                            │
  │    Set-Cookie: refresh_token│                            │
  │    Body: {accessToken}     │                            │
```

**Token Reuse Detection:**
- Nếu client gửi một refresh token đã bị revoke → hệ thống phát hiện tấn công session hijacking
- Tất cả refresh token của user bị revoke ngay lập tức (force logout toàn bộ sessions)
- Log cảnh báo ở mức `WARN`

---

### 2.5 Quên mật khẩu (Forgot Password Flow)

```
Step 1: Request OTP
  POST /auth/forgot-password {email}
    → Tạo OTP 6 số, lưu vào Redis (TTL: 5 phút)
    → Gửi email chứa OTP

Step 2: Verify OTP
  POST /auth/verify-reset-otp {email, otp}
    → Kiểm tra OTP trong Redis
    → Tạo reset token (UUID), lưu Redis (TTL: 15 phút)
    → Trả về {resetToken}

Step 3: Reset Password
  POST /auth/reset-password {resetToken, newPassword}
    → Verify reset token từ Redis
    → Hash và cập nhật mật khẩu mới
    → Revoke tất cả refresh token cũ (force logout)
    → Gửi email thông báo mật khẩu đã thay đổi
    → Xóa reset token khỏi Redis
```

---

### 2.6 Đăng xuất (Logout)

```
Client                      Backend                    Redis / DB
  │                            │                            │
  │── POST /auth/logout ───────►                            │
  │   Header: Authorization:   │                            │
  │     Bearer <accessToken>   │                            │
  │   Cookie: refresh_token    │                            │
  │                            │── Blacklist access token ─►Redis (TTL = remaining)
  │                            │── Revoke refresh token ───►DB
  │                            │                            │
  │◄── 200 OK ─────────────────│                            │
  │    Set-Cookie:             │                            │
  │      refresh_token=; Max-Age=0                          │
  │      (Clear cookie)        │                            │
```

**Logout All (POST /auth/logout-all):**
- Revoke tất cả refresh token của user trong DB
- Các thiết bị khác sẽ không thể dùng silent refresh nữa

---

## 3. User Management Flows

### 3.1 Cập nhật hồ sơ (Update Profile)

- Endpoint: `PUT /api/v1/users/me`
- Yêu cầu: Access Token hợp lệ
- Chỉ cho phép cập nhật: `fullName`, `phoneNumber`
- Email không thể thay đổi (immutable)
- Username có thể cập nhật nếu unique

### 3.2 Đổi mật khẩu (Change Password)

```
POST /api/v1/users/me/password
{oldPassword, newPassword, confirmPassword}

1. Verify oldPassword với BCrypt
2. Hash newPassword
3. Update passwordChangedAt
4. Revoke tất cả refresh token (force logout all sessions)
5. Gửi email thông báo mật khẩu đã thay đổi
```

### 3.3 Tự hủy tài khoản (Deactivate Account)

```
DELETE /api/v1/users/me
{password}  ← Xác nhận bằng mật khẩu hiện tại

1. Verify password
2. Set status = DELETED (soft delete)
3. Revoke tất cả sessions
```

---

## 4. Admin Flows

### 4.1 Quản lý người dùng

| Endpoint | Permission | Mô tả |
|----------|-----------|-------|
| `GET /admin/users` | `USER_READ` | Tìm kiếm, lọc, phân trang danh sách user |
| `GET /admin/users/{id}` | `USER_READ` | Chi tiết user |
| `POST /admin/users` | `USER_CREATE` | Tạo user mới |
| `PUT /admin/users/{id}` | `USER_UPDATE` | Cập nhật fullName, phoneNumber, roles, emailVerified |
| `PATCH /admin/users/{id}/status` | `USER_MANAGE_STATUS` | Thay đổi trạng thái (ACTIVE/LOCKED/INACTIVE) |
| `POST /admin/users/{id}/reset-password` | `USER_UPDATE` | Reset mật khẩu, gửi email thông báo |
| `POST /admin/users/{id}/force-logout` | `SESSION_REVOKE` | Revoke tất cả sessions của user |
| `DELETE /admin/users/{id}` | `USER_DELETE` | Soft delete user |
| `POST /admin/users/{id}/restore` | `USER_MANAGE_STATUS` | Khôi phục user đã xóa |
| `GET /admin/users/statistics` | `USER_READ` | Thống kê tổng quan |

### 4.2 Search & Filter Users

```
GET /admin/users?keyword=john&status=ACTIVE&page=0&size=10&sortBy=createdAt&sortDir=desc

Filter options:
- keyword: tìm theo email, username, fullName (ILIKE)
- status: ACTIVE | INACTIVE | LOCKED | DELETED
- roles: lọc theo tên role
- page/size: phân trang
- sortBy/sortDir: sắp xếp
```

### 4.3 Admin tạo user

```
POST /admin/users
{email, fullName, password, roles, phoneNumber, emailVerified}

1. Check email/username unique
2. Hash password
3. Set roles theo request
4. emailVerified theo request (admin có thể set = true)
```

---

## 5. Security & Token Architecture

### 5.1 JWT Token Structure

**Access Token:**
- TTL: 15 phút (dev), 5 phút (prod)
- Payload: `sub` (userId), `email`, `roles`, `permissions`, `jti`
- Lưu ở: Zustand store (in-memory, mất khi refresh page)

**Refresh Token:**
- TTL: 7 ngày
- Payload: `sub` (userId), `jti`
- Lưu ở: HttpOnly Secure Cookie (không truy cập được bằng JS)
- Được lưu vào bảng `token_validation` trong DB

### 5.2 Token Blacklist (Redis)

```
Khi access token bị blacklist (logout):
  Key: "blacklist:token:{jti}"
  Value: "REVOKED"
  TTL: thời gian còn lại của token

Khi startup: warm-up cache từ DB để đồng bộ
  → Load tất cả token chưa hết hạn từ DB vào Redis
```

### 5.3 Request Authorization Flow

```
Request ──► JwtAuthFilter
              │
              ├── Không có Bearer token? → Tiếp tục (public endpoint sẽ OK, protected sẽ 401)
              │
              ├── Validate JWT signature
              ├── Check token expiry
              ├── Check blacklist (Redis) ← nhanh, in-memory
              │
              ├── Extract userId, roles, permissions
              └── Set SecurityContext → Controller xử lý
```

### 5.4 CORS Configuration

```yaml
allowed-origins: http://localhost:5173  # Frontend dev
allowed-methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
allowed-headers: "*"
allow-credentials: true  # Cần thiết để cookie hoạt động cross-origin
```

---

## 6. Data Initializer (Startup Bootstrap)

Khi backend khởi động, `DataInitializer` tự động chạy một lần:

```
1. Tạo Permissions (nếu chưa tồn tại):
   USER_READ, USER_CREATE, USER_UPDATE, USER_DELETE, USER_MANAGE_STATUS,
   ROLE_READ, ROLE_CREATE, ROLE_UPDATE, ROLE_DELETE,
   PERMISSION_READ, PERMISSION_CREATE, PERMISSION_UPDATE, PERMISSION_DELETE,
   SESSION_REVOKE, ADMIN_ACCESS

2. Tạo Role ADMIN với tất cả permissions trên

3. Tạo Role USER với permissions cơ bản

4. Tạo tài khoản Admin bootstrap:
   - Email: ${ADMIN_EMAIL} (default: admin@example.com)
   - Password: ${ADMIN_PASSWORD} (default: Admin@12345)
   - Nếu đã tồn tại: cập nhật lại roles/permissions (idempotent)
```

---

## 7. Redis Data Structures

| Key Pattern | Value | TTL | Mục đích |
|-------------|-------|-----|----------|
| `email:verify:{email}` | OTP token | Cấu hình | Token xác thực email |
| `reset:otp:{email}` | OTP code 6 số | 5 phút | OTP quên mật khẩu |
| `reset:token:{email}` | Reset token UUID | 15 phút | Token đặt lại mật khẩu |
| `blacklist:token:{jti}` | "REVOKED" | Còn lại của token | Access token bị thu hồi |

---

## 8. Database Schema (Key Tables)

```
users
├── id (UUID, PK)
├── email (unique, not null)
├── username (unique)
├── password_hash
├── full_name
├── phone_number
├── auth_provider (LOCAL | GOOGLE)
├── provider_id
├── email_verified (boolean)
├── status (ACTIVE | INACTIVE | LOCKED | PENDING_VERIFICATION | DELETED)
├── last_login_at
├── password_changed_at
└── [audit: created_at, created_by, modified_at, modified_by]

roles: id, name, description, system_role, status
permissions: id, name, resource, action, description, status
user_roles: user_id, role_id (ManyToMany)
role_permissions: role_id, permission_id (ManyToMany)

token_validation
├── jti (VARCHAR, PK)
├── user_id (FK → users)
├── token_type (ACCESS | REFRESH)
├── expires_at
├── revoked_at
└── reason
```

---

## 9. Error Codes

| HTTP Code | App Code | Mô tả |
|-----------|----------|-------|
| 400 | 2001 | Validation failed (trường bắt buộc thiếu/sai format) |
| 401 | 4001 | Unauthorized (token không hợp lệ hoặc hết hạn) |
| 401 | 4002 | Invalid credentials (sai email/password) |
| 401 | 4003 | Token đã bị thu hồi |
| 403 | 4031 | Forbidden (không đủ quyền) |
| 404 | 4041 | User not found |
| 409 | 4091 | Email đã tồn tại |
| 409 | 4092 | Username đã tồn tại |
| 422 | 4221 | Email chưa được xác thực |
| 423 | 4231 | Tài khoản bị khóa |
| 500 | 5001 | Internal server error |

---

## 10. API Contract (Frontend ↔ Backend)

### Login Request/Response

```typescript
// Request (gửi lên)
{ usernameOrEmail: string, password: string }

// Response (nhận về)
{
  accessToken: string,       // JWT, lưu in-memory
  refreshToken: string,      // Đồng thời set qua Set-Cookie HttpOnly
  user: UserProfileResponse
}
```

### Register Request/Response

```typescript
// Request
{ email, password, confirmPassword, fullName, phoneNumber? }

// Response
{
  userId: string,
  email: string,
  fullName: string,
  emailVerificationRequired: boolean
}
```

---

## 11. Environment Variables

| Variable | Default (dev) | Mô tả |
|----------|--------------|-------|
| `DB_NAME` | `authdb` | PostgreSQL database name |
| `DB_USERNAME` | `postgres` | PostgreSQL user |
| `DB_PASSWORD` | `postgres` | PostgreSQL password |
| `DB_PORT` | `5433` | PostgreSQL port (dev dùng 5433 tránh conflict) |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | `redis` | Redis password |
| `JWT_SECRET` | — | **Bắt buộc**, Base64 encoded, ≥64 bytes |
| `MAIL_HOST` | `localhost` | SMTP host |
| `MAIL_PORT` | `1025` | SMTP port (MailHog) |
| `ADMIN_EMAIL` | `admin@example.com` | Bootstrap admin email |
| `ADMIN_PASSWORD` | `Admin@12345` | Bootstrap admin password |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Frontend origin |
