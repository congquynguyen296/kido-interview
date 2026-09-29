# KẾ HOẠCH TRIỂN KHAI: AUTHENTICATION & USER MANAGEMENT SERVICE

> Tài liệu này dành cho AI Agent thực thi. Hãy đọc **toàn bộ** trước khi viết code, sau đó làm **tuần tự theo từng Phase** ở mục 14. Hoàn thành và tự kiểm tra (build + test) xong Phase N mới chuyển sang Phase N+1.

---

## 0. QUY TẮC LÀM VIỆC CHO AI (ĐỌC TRƯỚC)

1. Bộ khung Maven đã tồn tại. **Đọc `pom.xml` trước**, lấy `groupId` làm base package (dưới đây ký hiệu là `{base}`). Không tạo project mới.
2. Chỉ thêm dependency còn thiếu vào `pom.xml`, không đổi version của parent Spring Boot. Đồng thời xóa đi các dependency không cần thiết ra khỏi file
3. **Tuyệt đối không hard-code**: HTTP code, thời gian (duration), secret, redirect URI, URL, tên cookie, tên header, key Redis, tên role/permission, regex, kích thước trang… Tất cả nằm ở `constant/` (giá trị bất biến của nghiệp vụ) hoặc `application-*.yml` + `@ConfigurationProperties` (giá trị phụ thuộc môi trường).
4. **Không có logic ở controller.** Controller chỉ: nhận request → `@Valid` → gọi 1 method service → bọc `ApiResponse` → trả về.
5. Mỗi API có **request class và response class riêng** (kể cả khi giống nhau). Không dùng entity làm request/response.
6. Mapper viết **tay** (class `@Component` thuần, không MapStruct).
7. Sau mỗi Phase: `mvn clean verify` (backend)
8. Nếu gặp điểm mơ hồ, chọn phương án được ghi trong tài liệu này; không tự ý đổi kiến trúc.

---

## 1. TỔNG QUAN

### 1.1 Mục tiêu
Xây dựng một service xác thực và quản lý người dùng đầy đủ, gồm backend REST API và frontend với REACT (đã có, tham khảo frontend)

### 1.2 Tech stack

| Lớp | Công nghệ |
|---|---|
| Backend | Java 21 (hoặc theo pom), Spring Boot 3.x, Spring Web, Spring Data JPA, Spring Security 6, Spring OAuth2 Client, Spring Validation, Spring Data Redis, Spring Mail, Lombok |
| JWT | `io.jsonwebtoken:jjwt` (api + impl + jackson) — hoặc `nimbus-jose-jwt` (đã đi kèm oauth2-resource-server). Chọn **jjwt** |
| DB | PostgreSQL 16 + **Flyway** để quản lý migration |
| Cache/Blacklist/OTP | Redis 7 |
| Tài liệu API | springdoc-openapi (Swagger UI, chỉ bật ở dev) |
| Test | JUnit 5, Mockito, Testcontainers (Postgres + Redis) |
| Mail | Spring Mail + Thymeleaf template; dev dùng MailHog |
| Frontend | React 18 + TypeScript + Vite, TailwindCSS, React Router v6, TanStack Query, Axios, React Hook Form + Zod, Zustand |
| DevOps | Docker, Docker Compose, Nginx (serve frontend) |

### 1.3 Quyết định kiến trúc cố định
- **Access token**: JWT, TTL ngắn (cấu hình yaml), trả trong body, frontend giữ **trong memory** (Zustand, không localStorage).
- **Refresh token**: JWT, TTL dài, lưu trong **HttpOnly cookie** (tên cookie, `secure`, `sameSite`, `path`, `domain` đều lấy từ yaml).
- **Refresh token rotation**: mỗi lần refresh sinh cặp token mới, token cũ bị vô hiệu. Nếu phát hiện refresh token đã bị vô hiệu bị dùng lại (reuse) → thu hồi toàn bộ phiên của user đó.
- **Đăng nhập Google**: Authorization Code flow bằng Spring `oauth2Login`. Sau khi thành công, backend set refresh cookie rồi redirect về `frontend redirect uri` (lấy từ yaml). Frontend gọi `/auth/refresh` để lấy access token. **Không đưa token lên URL.**
- **Token bị vô hiệu** lưu ở 2 nơi: Redis (tra cứu nhanh, TTL = thời gian sống còn lại của token) và bảng `token_validation` (bền vững, có job dọn token hết hạn). Khi app khởi động, nạp lại (warm-up) từ DB lên Redis.
- **Vô hiệu hoá toàn bộ phiên của một user** (đổi mật khẩu, bị khóa, admin force-logout, reuse detection): ghi `auth:user-invalidated-at:{userId} = epochMillis` vào Redis (TTL = refresh TTL). JWT filter từ chối mọi token có `iat` nhỏ hơn mốc này.
- **Xoá mềm** (soft delete) qua `status` của `BaseEntity`, không xoá cứng user.
- **OTP quên mật khẩu / xác thực email** lưu ở Redis (có TTL), **không** tạo thêm entity.

### 1.4 Các nguồn bổ sung để có thể implement tính năng tốt hơn:
- **Mail**: GMAIL_USER=congquynguyen296@gmail.com + GMAIL_PASS=gmwonyobgtbncgsn
- **Google Oauth**: GOOGLE_OAUTH_CLIENT_ID=your-google-client-id + GOOGLE_OAUTH_CLIENT_SECRET=your-google-client-secret + GOOGLE_OAUTH_REDIRECT=http://localhost:5173/auth/google/callback
---

## 2. CẤU TRÚC THƯ MỤC

### 2.1 Repository root
```
.
├── backend/
│   ├── src/main/java/{base}/...
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   ├── application-prod.yml
│   │   ├── db/migration/V1__init_schema.sql ...
│   │   └── templates/mail/*.html
│   ├── src/test/...
│   ├── Dockerfile
│   ├── .dockerignore
│   ├── .gitignore
│   ├── pom.xml
│   └── skill.md
├── frontend/
│   ├── src/...
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── .dockerignore
│   ├── .gitignore
│   ├── .env.example
│   └── skill.md
├── .agent/skills/                # bản sao skill cho Google Antigravity (xem mục 12)
├── docker-compose.yml            # full stack
├── docker-compose.dev.yml        # chỉ hạ tầng (postgres, redis, mailhog) cho dev
├── .env.example
├── .gitignore
└── README.md
```

### 2.2 Backend package layout (`{base}`)
```
config/         SecurityConfig, JwtProperties, CorsProperties, OAuth2Properties, MailProperties,
                OtpProperties, AuthProperties, AdminBootstrapProperties, RedisConfig, JpaAuditingConfig,
                AsyncConfig, OpenApiConfig, DataInitializer, JwtAuthenticationFilter,
                CustomAuthenticationEntryPoint, CustomAccessDeniedHandler,
                OAuth2SuccessHandler, OAuth2FailureHandler, AuditorAwareImpl
constant/       HTTPCode, PredefinedRole, PredefinedPermission, TokenType, AuthProvider,
                EntityStatus, Gender, RedisKey, AppConstant, ApiPath, MessageKey, OtpPurpose, MailTemplate
controller/     AuthController, UserController (me), AdminUserController,
                AdminRoleController, AdminPermissionController
dto/
  common/       ApiResponse, PageResponse, PageRequestParam
  request/{auth,user,admin}/     mỗi API 1 class
  response/{auth,user,admin}/    mỗi API 1 class
entity/         BaseEntity, User, Role, Permission, TokenValidation
exception/      AppException, GlobalExceptionHandler
repository/     UserRepository, RoleRepository, PermissionRepository, TokenValidationRepository
service/        (interface) + impl/  : AuthService, UserService, AdminUserService, RoleService,
                PermissionService, JwtService, TokenBlacklistService, OtpService, MailService,
                LoginAttemptService, CurrentUserService
mapper/         UserMapper, RoleMapper, PermissionMapper (viết tay)
scheduler/      ExpiredTokenCleanupScheduler   (tuỳ chọn: đặt trong service/)
```

Note: Kiểm tra lại frontend. Hiện tại đang sử dụng mock data. Nếu còn thiếu phần nào thì thêm vào

---

## 3. CẤU HÌNH YAML (KHÔNG HARD-CODE)

Tạo 3 file. Mọi giá trị nhạy cảm dùng biến môi trường, `application-prod.yml` **không có default** cho secret.

### 3.1 `application.yml` (chung)
```yaml
spring:
  application:
    name: auth-service
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  jpa:
    open-in-view: false
    properties:
      hibernate:
        jdbc.time_zone: UTC
  jackson:
    default-property-inclusion: non_null
  flyway:
    enabled: true
    locations: classpath:db/migration
```

### 3.2 `application-dev.yml`
Nội dung cần có (giá trị dev cho phép default để chạy nhanh):
```yaml
server:
  port: ${SERVER_PORT:8080}
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:authdb}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
  jpa:
    hibernate.ddl-auto: validate
    show-sql: true
  data.redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
  mail:
    host: ${MAIL_HOST:localhost}
    port: ${MAIL_PORT:1025}          # MailHog
  security.oauth2.client.registration.google:
    client-id: ${GOOGLE_CLIENT_ID}
    client-secret: ${GOOGLE_CLIENT_SECRET}
    scope: [openid, profile, email]
    redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"

app:
  jwt:
    secret: ${JWT_SECRET:<chuỗi-dev-đủ-dài-64-ký-tự>}
    issuer: ${JWT_ISSUER:auth-service}
    access-token-ttl: 15m
    refresh-token-ttl: 7d
    clock-skew: 30s
  cookie:
    refresh-token-name: refresh_token
    path: /api/v1/auth
    secure: false
    same-site: Lax
    http-only: true
    domain:                          # để trống ở dev
  cors:
    allowed-origins: [http://localhost:5173]
    allowed-methods: [GET, POST, PUT, PATCH, DELETE, OPTIONS]
    allowed-headers: ["*"]
    allow-credentials: true
    max-age: 1h
  oauth2:
    frontend-success-redirect-uri: http://localhost:5173/oauth2/callback
    frontend-failure-redirect-uri: http://localhost:5173/login?error=oauth2
  otp:
    length: 6
    ttl: 5m
    max-attempts: 5
    resend-cooldown: 60s
    reset-token-ttl: 10m
    email-verification-ttl: 24h
  auth:
    max-failed-login-attempts: 5
    lock-duration: 15m
    failed-attempt-window: 15m
    bcrypt-strength: 10
    password-min-length: 8
    require-email-verification: false   # dev = false, prod = true
    public-endpoints: [/api/v1/auth/**, /oauth2/**, /login/oauth2/**, /swagger-ui/**, /v3/api-docs/**, /actuator/health]
  admin-bootstrap:
    enabled: true
    email: ${ADMIN_EMAIL:admin@example.com}
    password: ${ADMIN_PASSWORD:Admin@12345}
    full-name: System Administrator
  mail:
    from: no-reply@example.com
    from-name: Auth Service
    frontend-reset-password-url: http://localhost:5173/reset-password
    frontend-verify-email-url: http://localhost:5173/verify-email
  pagination:
    default-page-size: 10
    max-page-size: 100
  springdoc.enabled: true
```

Note: Giải thích các biến dùng để làm gì. Sử dụng cấu trúc comment '#' ở cùng dòng và ngay phía sau

### 3.3 `application-prod.yml`
- Cùng cấu trúc, nhưng: **không default** cho `DB_*`, `REDIS_*`, `JWT_SECRET`, `GOOGLE_*`, `MAIL_*`, `ADMIN_*`.
- `spring.jpa.show-sql: false`, `springdoc.enabled: false`.
- `app.cookie.secure: true`, `same-site: Strict` (hoặc `None` nếu FE khác domain — cấu hình qua env), `domain: ${COOKIE_DOMAIN}`.
- `app.cors.allowed-origins: ${CORS_ALLOWED_ORIGINS}`.
- `app.oauth2.*` và `app.mail.frontend-*` lấy từ env (`FRONTEND_BASE_URL`…).
- `app.auth.require-email-verification: true`.
- `app.admin-bootstrap.enabled: ${ADMIN_BOOTSTRAP_ENABLED:false}`.
- Bật `server.forward-headers-strategy: framework` (chạy sau reverse proxy) và `management.endpoints.web.exposure.include: health`.

### 3.4 Các class `@ConfigurationProperties`
Mỗi nhóm `app.*` map sang 1 class (`@ConfigurationProperties(prefix = "app.jwt")`, dùng `Duration`, `@Validated`, `@NotBlank`/`@NotNull`). Đăng ký bằng `@ConfigurationPropertiesScan`. `JwtProperties.secret` phải kiểm tra độ dài tối thiểu (≥ 32 byte) lúc khởi động, sai thì fail-fast.

---

## 4. CONSTANT (`constant/`)

### 4.1 `HTTPCode` (enum, dùng trong `ApiResponse`)
Mỗi phần tử gồm: `int code`, `String message` (hoặc messageKey), `HttpStatus httpStatus`.

| Nhóm | Ví dụ phần tử |
|---|---|
| Thành công | `SUCCESS(1000, OK)`, `CREATED(1001, CREATED)`, `NO_CONTENT` |
| Validation | `INVALID_REQUEST(2000, 400)`, `VALIDATION_FAILED(2001, 400)`, `MALFORMED_JSON(2002, 400)` |
| Xác thực | `UNAUTHENTICATED(3000, 401)`, `INVALID_CREDENTIALS(3001, 401)`, `TOKEN_EXPIRED(3002, 401)`, `TOKEN_INVALID(3003, 401)`, `TOKEN_REVOKED(3004, 401)`, `REFRESH_TOKEN_MISSING(3005, 401)`, `ACCOUNT_LOCKED(3006, 423)`, `ACCOUNT_DISABLED(3007, 403)`, `EMAIL_NOT_VERIFIED(3008, 403)` |
| Phân quyền | `FORBIDDEN(4000, 403)` |
| Không tìm thấy | `USER_NOT_FOUND(5000, 404)`, `ROLE_NOT_FOUND`, `PERMISSION_NOT_FOUND` |
| Xung đột | `EMAIL_ALREADY_EXISTS(6000, 409)`, `USERNAME_ALREADY_EXISTS`, `ROLE_ALREADY_EXISTS`, `PERMISSION_ALREADY_EXISTS`, `ROLE_IN_USE(409)` |
| Mật khẩu | `PASSWORD_MISMATCH(7000, 400)`, `OLD_PASSWORD_INCORRECT`, `NEW_PASSWORD_SAME_AS_OLD`, `WEAK_PASSWORD` |
| OTP | `OTP_INVALID(8000, 400)`, `OTP_EXPIRED`, `OTP_TOO_MANY_ATTEMPTS(429)`, `OTP_RESEND_TOO_SOON(429)`, `RESET_TOKEN_INVALID` |
| Hệ thống | `INTERNAL_ERROR(9000, 500)`, `TOO_MANY_REQUESTS(9001, 429)`, `OPERATION_NOT_ALLOWED(9002, 403)` |

Yêu cầu:
- `ApiResponse.code` phải có **giá trị mặc định** `HTTPCode.SUCCESS` (`@Builder.Default private HTTPCode code = HTTPCode.SUCCESS;`). Bản mẫu của người dùng đang thiếu khởi tạo, hãy sửa.
- Serialize `code` ra JSON dạng **số** (`@JsonValue` trên getter `getCode()` của enum).
- Thông điệp lấy từ `HTTPCode.message`, có thể override qua `ApiResponse.message`.

### 4.2 Các constant khác
- `PredefinedRole`: `ADMIN`, `USER`, `MODERATOR` (không có tiền tố `ROLE_` trong DB; thêm tiền tố khi tạo `GrantedAuthority`, tiền tố đặt trong `AppConstant.ROLE_PREFIX`).
- `PredefinedPermission`: dạng `RESOURCE_ACTION`, ví dụ `USER_READ`, `USER_CREATE`, `USER_UPDATE`, `USER_DELETE`, `USER_MANAGE_STATUS`, `USER_ASSIGN_ROLE`, `ROLE_READ`, `ROLE_CREATE`, `ROLE_UPDATE`, `ROLE_DELETE`, `PERMISSION_READ`, `PERMISSION_CREATE`, `PERMISSION_UPDATE`, `PERMISSION_DELETE`, `SESSION_REVOKE`. Kèm map mặc định role → permissions dùng cho `DataInitializer` (ADMIN: tất cả; USER: không có permission quản trị; MODERATOR: `USER_READ`, `USER_MANAGE_STATUS`).
- `TokenType`: `ACCESS`, `REFRESH`.
- `AuthProvider`: `LOCAL`, `GOOGLE`.
- `EntityStatus`: `ACTIVE`, `INACTIVE`, `LOCKED`, `PENDING_VERIFICATION`, `DELETED`.
- `Gender`: `MALE`, `FEMALE`, `OTHER`.
- `OtpPurpose`: `PASSWORD_RESET`, `EMAIL_VERIFICATION`.
- `RedisKey`: các prefix/template: `auth:blacklist:%s`, `auth:user-invalidated-at:%s`, `auth:login-fail:%s`, `auth:lock:%s`, `auth:otp:%s:%s`, `auth:otp-attempts:%s:%s`, `auth:otp-cooldown:%s:%s`, `auth:reset-token:%s`.
- `AppConstant`: `AUTH_HEADER = "Authorization"`, `BEARER_PREFIX = "Bearer "`, `ROLE_PREFIX`, các claim name (`CLAIM_TYPE`, `CLAIM_ROLES`, `CLAIM_PERMISSIONS`, `CLAIM_JTI`…), sort mặc định (`DEFAULT_SORT_FIELD = "createdAt"`).
- `ApiPath`: hằng số đường dẫn: `API_V1 = "/api/v1"`, `AUTH = "/auth"`, `USERS = "/users"`, `ADMIN = "/admin"`, …
- `MailTemplate`: tên template + subject key.

---

## 5. ENTITY & DATABASE

### 5.1 `BaseEntity` (`@MappedSuperclass`, `@EntityListeners(AuditingEntityListener.class)`)

| Field | Kiểu | Mặc định / ràng buộc |
|---|---|---|
| `id` | `UUID` | `@GeneratedValue(strategy = GenerationType.UUID)` |
| `createdAt` | `Instant` | `@CreatedDate`, `updatable=false`, mặc định `Instant.now()` |
| `createdBy` | `String` | `@CreatedBy`, nullable (do hệ thống/đăng ký tự do không có người tạo) |
| `modifiedAt` | `Instant` | `@LastModifiedDate`, mặc định `Instant.now()` |
| `modifiedBy` | `String` | `@LastModifiedBy`, nullable |
| `status` | `EntityStatus` | `@Enumerated(STRING)`, `@Builder.Default = ACTIVE`, not null |

Dùng `@SuperBuilder`, `@Getter/@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`. `AuditorAwareImpl` trả `userId` (hoặc email) từ `SecurityContext`, nếu không có thì `"SYSTEM"`.

### 5.2 `User` (bảng `users`)
`email` (unique, lowercase, not null), `username` (unique, nullable), `passwordHash` (nullable vì tài khoản Google không có mật khẩu), `fullName`, `phoneNumber`, `dateOfBirth`, `gender`, `authProvider` (default `LOCAL`), `providerId` (nullable), `emailVerified` (default `false`), `lastLoginAt` (nullable), `passwordChangedAt` (nullable), `roles` (`@ManyToMany(fetch = LAZY)` qua bảng `user_roles`, `@Builder.Default new HashSet<>()`).
- Index: `email`, `username`, `(auth_provider, provider_id)`, `status`.

### 5.3 `Role` (bảng `roles`)
`name` (unique, not null — dùng giá trị của `PredefinedRole`), `description`, `permissions` (`@ManyToMany` qua `role_permissions`), cờ `systemRole` (boolean, default `false`; role hệ thống không được xoá/đổi tên).

### 5.4 `Permission` (bảng `permissions`)
`name` (unique, not null), `description`, `resource` (nullable), `action` (nullable).

### 5.5 `TokenValidation` (bảng `token_validation`) — lưu token đã bị vô hiệu
`jti` (unique, not null, index), `tokenType` (`ACCESS`/`REFRESH`), `userId` (UUID, index), `expiresAt` (`Instant`, index — dùng để dọn dẹp), `revokedAt` (default now), `reason` (String, nullable: `LOGOUT`, `ROTATED`, `PASSWORD_CHANGED`, `ADMIN_REVOKED`, `REUSE_DETECTED`).

### 5.6 Flyway
- `V1__init_schema.sql`: tạo toàn bộ bảng, khoá ngoại, unique, index.
- `ddl-auto: validate` ở cả dev và prod (schema do Flyway quản lý).
- Seed dữ liệu (role, permission, admin) làm trong `DataInitializer` (idempotent), **không** seed bằng SQL để tránh hard-code mật khẩu.

---

## 6. BẢO MẬT & LUỒNG TOKEN

### 6.1 JWT
Claims: `sub = userId`, `jti = UUID`, `iss`, `iat`, `exp`, `type = ACCESS|REFRESH`. Access token thêm `roles` và `permissions` (danh sách string). Refresh token **không** chứa roles/permissions. Ký HS512 với secret từ `JwtProperties`.

`JwtService`: `generateAccessToken(User)`, `generateRefreshToken(User)`, `parse(token, expectedType)`, `getRemainingTtl(claims)`.

### 6.2 `JwtAuthenticationFilter` (`OncePerRequestFilter`)
1. Đọc header `Authorization` (`Bearer `). Không có → cho đi tiếp (để endpoint public hoạt động).
2. Parse + verify chữ ký, hạn, `type == ACCESS`.
3. Kiểm tra `jti` trong Redis blacklist.
4. Kiểm tra `iat >= user-invalidated-at`.
5. Dựng `Authentication` từ claims (không query DB mỗi request), authorities = `ROLE_x` + các permission.
6. Lỗi → để `CustomAuthenticationEntryPoint` trả `ApiResponse` với `HTTPCode` tương ứng (`TOKEN_EXPIRED`, `TOKEN_INVALID`, `TOKEN_REVOKED`).

### 6.3 `SecurityConfig`
- Stateless (`SessionCreationPolicy.STATELESS`) — riêng luồng OAuth2 cần lưu request tạm: dùng `HttpSessionOAuth2AuthorizationRequestRepository` hoặc cookie-based repository tự viết; chọn **cookie-based** để giữ stateless.
- CSRF tắt cho API (dùng Bearer); riêng `/api/v1/auth/refresh` và `/logout` dùng cookie nên bắt buộc `SameSite` + kiểm tra header `Origin` nằm trong `app.cors.allowed-origins`.
- CORS lấy từ `CorsProperties`.
- `permitAll` cho `app.auth.public-endpoints`, còn lại `authenticated()`.
- `@EnableMethodSecurity`; dùng `@PreAuthorize("hasAuthority('USER_READ')")` trên **service** (hoặc controller cho annotation phân quyền, không chứa logic). Tên permission lấy từ `PredefinedPermission` (dùng constant string trong annotation).
- `PasswordEncoder` = `BCryptPasswordEncoder(strength từ AuthProperties)`.
- `oauth2Login` với `successHandler`/`failureHandler` tự viết.
- Security headers mặc định của Spring Security giữ nguyên.

### 6.4 Luồng chính
**Login**: kiểm tra khoá tạm (Redis) → tìm user theo email → so mật khẩu → sai thì tăng bộ đếm, đủ `max-failed-login-attempts` thì khoá `lock-duration` → đúng thì reset bộ đếm, kiểm tra `status` (LOCKED/INACTIVE/DELETED/PENDING) và `emailVerified` nếu `require-email-verification` → cập nhật `lastLoginAt` → sinh access + refresh → set refresh cookie → trả `LoginResponse` (accessToken, tokenType, expiresIn, user).

**Refresh**: đọc refresh token từ cookie → parse (`type == REFRESH`) → nếu `jti` đã bị vô hiệu ⇒ **reuse detection**: ghi `user-invalidated-at`, thu hồi, trả `TOKEN_REVOKED` → nếu hợp lệ: vô hiệu `jti` cũ (reason `ROTATED`), sinh cặp mới, set cookie mới, trả `RefreshTokenResponse`.

**Logout**: vô hiệu `jti` của access token hiện tại (từ header) và refresh token (từ cookie, nếu có) → xoá cookie (Max-Age=0) . **Logout-all**: ghi `user-invalidated-at = now`.

**Google login**: `OAuth2SuccessHandler` lấy `email`, `sub`, `name`, `picture`, `email_verified` → nếu chưa có user: tạo user (`authProvider=GOOGLE`, `emailVerified=true`, role `USER`, `passwordHash=null`); nếu đã có user LOCAL cùng email: liên kết (set `providerId`) và giữ nguyên đăng nhập bằng mật khẩu → kiểm tra status → sinh refresh token, set cookie → redirect về `app.oauth2.frontend-success-redirect-uri`. Lỗi → redirect `frontend-failure-redirect-uri`.

**Quên mật khẩu (3 bước)**:
1. `forgot-password`: nhận email → **luôn trả thông điệp thành công chung** (không lộ email có tồn tại hay không) → nếu user tồn tại và là LOCAL: kiểm tra cooldown, sinh OTP (`SecureRandom`, độ dài từ yaml), lưu **hash** OTP vào Redis với TTL, gửi mail bất đồng bộ.
2. `verify-reset-otp`: nhận email + OTP → đếm số lần thử (vượt `max-attempts` thì xoá OTP + trả `OTP_TOO_MANY_ATTEMPTS`) → đúng thì xoá OTP, sinh `resetToken` (UUID/random 256-bit) lưu Redis TTL `reset-token-ttl`, trả `resetToken`.
3. `reset-password`: nhận `resetToken` + mật khẩu mới + xác nhận → cập nhật hash, `passwordChangedAt`, xoá `resetToken`, ghi `user-invalidated-at` (đăng xuất mọi thiết bị) → gửi mail thông báo.

**Đổi mật khẩu**: kiểm tra mật khẩu cũ, mới ≠ cũ, mới = xác nhận → cập nhật → `user-invalidated-at` → cấp lại cặp token mới cho phiên hiện tại (tuỳ chọn) hoặc yêu cầu đăng nhập lại (**chọn: yêu cầu đăng nhập lại**, trả 200 và xoá cookie).

**Xác thực email**: khi đăng ký sinh token/OTP `EMAIL_VERIFICATION` (TTL `email-verification-ttl`), gửi mail chứa link `frontend-verify-email-url?email=...&token=...`. `verify-email` xác nhận, đặt `emailVerified=true` và `status=ACTIVE`. Nếu `require-email-verification=false` thì user ACTIVE ngay khi đăng ký nhưng vẫn gửi mail xác thực.

**Bảo vệ tự thân cho admin**: admin không được tự khoá/xoá chính mình, không được gỡ role ADMIN cuối cùng của hệ thống (`OPERATION_NOT_ALLOWED`).

Note: Đối với case đăng nhập với google: Lưu ý khi gửi mail chào mừng thì phải đính kèm password mặc định và yêu cầu user đối mật khẩu. Luồng đổi mật sẽ theo API reset-password

---

## 7. DANH SÁCH API

Base path lấy từ `ApiPath` (`/api/v1`). Tất cả response bọc trong `ApiResponse<T>`. Mỗi API **bắt buộc** có cặp `XxxRequest` / `XxxResponse` riêng (ghi ở cột 4); API không có body thì không cần request class, nhưng vẫn phải có response class nếu trả dữ liệu.

### 7.1 Auth (`AuthController`, public trừ khi ghi chú)

| # | Method & Path | Mô tả | Request | Response |
|---|---|---|---|---|
| 1 | `POST /auth/register` | Đăng ký tài khoản LOCAL, gán role `USER` | `RegisterRequest` (email, password, confirmPassword, fullName, username?, phoneNumber?) | `RegisterResponse` (userId, email, fullName, emailVerificationRequired) |
| 2 | `POST /auth/verify-email` | Xác thực email | `VerifyEmailRequest` (email, token) | `VerifyEmailResponse` |
| 3 | `POST /auth/resend-verification` | Gửi lại mail xác thực | `ResendVerificationRequest` (email) | `ResendVerificationResponse` |
| 4 | `POST /auth/login` | Đăng nhập | `LoginRequest` (email hoặc username, password) | `LoginResponse` (accessToken, tokenType, expiresIn, user: `UserProfileResponse`) + set cookie refresh |
| 5 | `POST /auth/refresh` | Đổi cặp token (cookie) | *(không body)* | `RefreshTokenResponse` (accessToken, tokenType, expiresIn) + set cookie mới |
| 6 | `POST /auth/logout` *(auth)* | Đăng xuất thiết bị hiện tại | *(không body)* | `LogoutResponse` |
| 7 | `POST /auth/logout-all` *(auth)* | Đăng xuất mọi thiết bị | *(không body)* | `LogoutAllResponse` |
| 8 | `POST /auth/forgot-password` | Gửi OTP quên mật khẩu | `ForgotPasswordRequest` (email) | `ForgotPasswordResponse` (message chung, `resendAfterSeconds`) |
| 9 | `POST /auth/verify-reset-otp` | Xác thực OTP | `VerifyResetOtpRequest` (email, otp) | `VerifyResetOtpResponse` (resetToken, expiresIn) |
| 10 | `POST /auth/reset-password` | Đặt lại mật khẩu | `ResetPasswordRequest` (resetToken, newPassword, confirmPassword) | `ResetPasswordResponse` |
| 11 | `GET /oauth2/authorization/google` | Bắt đầu login Google (do Spring cung cấp, redirect) | — | redirect 302 |
| 12 | `GET /login/oauth2/code/google` | Callback Google (Spring xử lý → `OAuth2SuccessHandler`) | — | redirect 302 về FE |

### 7.2 Người dùng hiện tại (`UserController`, yêu cầu đăng nhập)

| # | Method & Path | Mô tả | Request | Response |
|---|---|---|---|---|
| 13 | `GET /users/me` | Lấy hồ sơ | — | `UserProfileResponse` (id, email, username, fullName, phoneNumber, dateOfBirth, gender, authProvider, emailVerified, roles, permissions, createdAt, lastLoginAt) |
| 14 | `PUT /users/me` | Cập nhật hồ sơ | `UpdateProfileRequest` | `UpdateProfileResponse` |
| 16 | `PATCH /users/me/password` | Đổi mật khẩu (chỉ LOCAL) | `ChangePasswordRequest` (oldPassword, newPassword, confirmPassword) | `ChangePasswordResponse` |
| 17 | `DELETE /users/me` | Vô hiệu hoá tài khoản của mình (xác nhận bằng mật khẩu với LOCAL) | `DeactivateAccountRequest` (password?) | `DeactivateAccountResponse` |

### 7.3 Quản trị người dùng (`AdminUserController`, prefix `/admin/users`)

| # | Method & Path | Permission | Request | Response |
|---|---|---|---|---|
| 18 | `GET /admin/users` | `USER_READ` | query: `AdminUserSearchRequest` (keyword, status, role, provider, emailVerified, createdFrom, createdTo, page, size, sort) | `PageResponse<AdminUserListItemResponse>` |
| 19 | `GET /admin/users/{id}` | `USER_READ` | — | `AdminUserDetailResponse` (kèm roles, permissions, audit fields) |
| 20 | `POST /admin/users` | `USER_CREATE` | `AdminCreateUserRequest` (email, password?, fullName, roles, status, emailVerified) | `AdminCreateUserResponse` |
| 21 | `PUT /admin/users/{id}` | `USER_UPDATE` | `AdminUpdateUserRequest` | `AdminUpdateUserResponse` |
| 22 | `PATCH /admin/users/{id}/status` | `USER_MANAGE_STATUS` | `AdminChangeUserStatusRequest` (status, reason?) | `AdminChangeUserStatusResponse` (khi LOCKED/INACTIVE/DELETED → vô hiệu mọi phiên) |
| 24 | `POST /admin/users/{id}/reset-password` | `USER_UPDATE` | `AdminResetPasswordRequest` (newPassword? — bỏ trống thì sinh mật khẩu ngẫu nhiên và gửi mail) | `AdminResetPasswordResponse` |
| 25 | `POST /admin/users/{id}/force-logout` | `SESSION_REVOKE` | — | `AdminForceLogoutResponse` |
| 26 | `DELETE /admin/users/{id}` | `USER_DELETE` | — | `AdminDeleteUserResponse` (soft delete: `status=DELETED`) |
| 27 | `POST /admin/users/{id}/restore` | `USER_MANAGE_STATUS` | — | `AdminRestoreUserResponse` |
| 28 | `GET /admin/users/statistics` | `USER_READ` | — | `AdminUserStatisticsResponse` (tổng, theo status, theo provider, đăng ký 7/30 ngày gần nhất) |

[//]: # (### 7.4 Quản trị Role & Permission)

[//]: # ()
[//]: # (| # | Method & Path | Permission | Request | Response |)

[//]: # (|---|---|---|---|---|)

[//]: # (| 29 | `GET /admin/roles` | `ROLE_READ` | — | `List<RoleResponse>` &#40;bao gồm permissions&#41; |)

[//]: # (| 30 | `GET /admin/roles/{id}` | `ROLE_READ` | — | `RoleDetailResponse` |)

[//]: # (| 31 | `POST /admin/roles` | `ROLE_CREATE` | `CreateRoleRequest` &#40;name, description, permissionNames&#41; | `CreateRoleResponse` |)

[//]: # (| 32 | `PUT /admin/roles/{id}` | `ROLE_UPDATE` | `UpdateRoleRequest` | `UpdateRoleResponse` &#40;không đổi tên role hệ thống&#41; |)

[//]: # (| 33 | `PUT /admin/roles/{id}/permissions` | `ROLE_UPDATE` | `AssignPermissionsRequest` | `AssignPermissionsResponse` |)

[//]: # (| 34 | `DELETE /admin/roles/{id}` | `ROLE_DELETE` | — | `DeleteRoleResponse` &#40;chặn nếu là role hệ thống hoặc đang có user&#41; |)

[//]: # (| 35 | `GET /admin/permissions` | `PERMISSION_READ` | — | `List<PermissionResponse>` |)

[//]: # (| 36 | `POST /admin/permissions` | `PERMISSION_CREATE` | `CreatePermissionRequest` | `CreatePermissionResponse` |)

[//]: # (| 37 | `PUT /admin/permissions/{id}` | `PERMISSION_UPDATE` | `UpdatePermissionRequest` | `UpdatePermissionResponse` |)

[//]: # (| 38 | `DELETE /admin/permissions/{id}` | `PERMISSION_DELETE` | — | `DeletePermissionResponse` |)

### 7.4 Quy ước chung cho API
- Validate bằng Jakarta Validation. Message lỗi lấy từ **message key** (`ValidationMessages.properties` hoặc constant `MessageKey`), không viết chuỗi trực tiếp trong annotation.
- Mật khẩu: độ dài tối thiểu lấy từ `AuthProperties`; kiểm tra độ mạnh (chữ hoa, thường, số, ký tự đặc biệt) trong service (regex đặt ở constant).
- Phân trang trả `PageResponse<T>` (content, page, size, totalElements, totalPages, first, last). `size` bị giới hạn bởi `app.pagination.max-page-size`.
- Tìm kiếm động dùng `JpaSpecificationExecutor` (Specification viết tay trong repository package hoặc service).
- Chống N+1: dùng `@EntityGraph` khi tải user kèm roles/permissions.
- Mọi email chuẩn hoá `trim().toLowerCase()` ở service.

---

## 8. DTO CHUNG

```java
// dto/common/ApiResponse.java  (giữ nguyên bản người dùng đưa, chỉ bổ sung default và factory)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    @Builder.Default HTTPCode code = HTTPCode.SUCCESS;
    String message;
    T result;

    public static <T> ApiResponse<T> success(T result) { ... }              // message lấy từ HTTPCode
    public static <T> ApiResponse<T> success(HTTPCode code, T result) { ... }
    public static <T> ApiResponse<T> error(HTTPCode code) { ... }
    public static <T> ApiResponse<T> error(HTTPCode code, String message, T details) { ... }
}
```
- Lỗi validation: `result` là `List<FieldErrorResponse>` (field, message).
- Controller trả `ResponseEntity<ApiResponse<T>>` với HTTP status lấy từ `HTTPCode.getHttpStatus()`.
- Mọi request/response class dùng `@Data @Builder @NoArgsConstructor @AllArgsConstructor`, `@FieldDefaults(level = PRIVATE)`.

---

## 9. EXCEPTION

- `AppException extends RuntimeException` chứa `HTTPCode`.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) xử lý: `AppException`, `MethodArgumentNotValidException`, `ConstraintViolationException`, `HttpMessageNotReadableException`, `AuthenticationException`, `AccessDeniedException`, `DataIntegrityViolationException` (đua unique → 409), `Exception` (500, log stacktrace nhưng **không** trả stacktrace ra client).
- `CustomAuthenticationEntryPoint` và `CustomAccessDeniedHandler` ghi JSON `ApiResponse` trực tiếp vào response (dùng `ObjectMapper`).
- Không log mật khẩu, token, OTP ở bất kỳ mức log nào.

---

## 10. SERVICE (toàn bộ logic ở đây)

Mỗi service có **interface** + `impl/XxxServiceImpl`. `@Transactional` ở tầng service (`readOnly = true` cho truy vấn).

| Service | Trách nhiệm |
|---|---|
| `AuthService` | register, verifyEmail, resendVerification, login, refresh, logout, logoutAll, forgotPassword, verifyResetOtp, resetPassword, xử lý đăng nhập Google (được gọi từ `OAuth2SuccessHandler`) |
| `UserService` | getMe, updateProfile, updateAvatar, changePassword, deactivateAccount |
| `AdminUserService` | search, detail, create, update, changeStatus, assignRoles, resetPassword, forceLogout, softDelete, restore, statistics |

| `JwtService` | sinh/parse token |
| `TokenBlacklistService` | `blacklist(jti, type, userId, expiresAt, reason)`, `isBlacklisted(jti)`, `invalidateAllUserSessions(userId)`, `getUserInvalidatedAt(userId)`, warm-up khi khởi động (`ApplicationReadyEvent`) |
| `ExpiredTokenCleanupScheduler` | `@Scheduled` (cron lấy từ yaml `app.auth.token-cleanup-cron`) xoá `token_validation` đã hết hạn |
| `OtpService` | sinh/hash/kiểm tra OTP, cooldown, đếm số lần thử, resetToken |
| `MailService` | gửi mail HTML bất đồng bộ (`@Async`) qua Thymeleaf: verify-email, otp-reset, password-changed, admin-reset-password |
| `LoginAttemptService` | đếm lần sai, khoá/mở khoá qua Redis |
| `CurrentUserService` | lấy `userId`/user hiện tại từ `SecurityContext` |

`DataInitializer` (`ApplicationRunner`, idempotent): tạo permission từ `PredefinedPermission`, role từ `PredefinedRole` (đánh dấu `systemRole=true`), gán permission mặc định, tạo tài khoản admin từ `AdminBootstrapProperties` nếu `enabled` và chưa tồn tại.

---



## 12. FILE `skill.md` CHO AI (GOOGLE ANTIGRAVITY) - TUYỆT ĐỐI PHẢI CÓ DO QUAN TRỌNG

Tạo:
- `backend/skill.md`
- `frontend/skill.md`
- Bản sao cho Antigravity theo cấu trúc skill của workspace: `.agent/skills/backend-conventions/SKILL.md` và `.agent/skills/frontend-conventions/SKILL.md`, mỗi file bắt đầu bằng frontmatter:
  ```yaml
  ---
  name: backend-conventions
  description: Quy ước code, kiến trúc và checklist khi phát triển backend auth-service (Spring Boot). Dùng khi thêm/sửa API, entity, service, cấu hình.
  ---
  ```
  (Kiểm tra lại đường dẫn skill đúng theo phiên bản Antigravity đang dùng; nếu khác thì đặt đúng nơi nhưng vẫn giữ `backend/skill.md` và `frontend/skill.md`.)

### 12.1 Nội dung bắt buộc của `backend/skill.md`
1. Tổng quan dự án và tech stack.
2. Cây thư mục + trách nhiệm từng package; **luật phụ thuộc**: controller → service → repository; mapper chỉ gọi bởi service; controller không import repository/entity.
3. Danh sách quy ước: không hard-code (nêu nơi đặt từng loại giá trị), logic chỉ ở service, mỗi API 1 request + 1 response, mapper viết tay, `ApiResponse<T>` + `HTTPCode`, `BaseEntity`, đặt tên (Request/Response/Impl/Properties), Lombok (dùng gì, cấm gì: không `@Data` trên entity JPA, không `@ToString` gây lazy loading).
4. **Quy trình thêm API mới** (checklist từng bước): constant/HTTPCode → request/response DTO → service interface + impl → mapper → controller → phân quyền (`PredefinedPermission` + `DataInitializer`) → cấu hình yaml (nếu có) → migration Flyway (nếu đổi schema) → unit test → cập nhật OpenAPI/README.
5. **Quy trình thêm entity/migration**, thêm config property, thêm mã lỗi.
6. Quy tắc bảo mật: không log dữ liệu nhạy cảm, không trả thông tin thừa (user enumeration), luồng token, blacklist, nơi kiểm tra.
7. Quy tắc test (Mockito cho service, Testcontainers cho integration), cách chạy (`mvn`, profile `dev`).
8. Lệnh thường dùng, biến môi trường, và **Definition of Done**.
9. Danh sách "KHÔNG ĐƯỢC LÀM" (anti-patterns).

### 12.2 Nội dung bắt buộc của `frontend/skill.md`
1. Tổng quan, stack, cấu trúc thư mục, luật phụ thuộc (page → hook → api; component không gọi axios trực tiếp).
2. Quy ước: constants, types, schemas zod, đặt tên, TypeScript strict, không `any`.
3. Luồng auth phía FE (silent refresh, interceptor, guard, lưu token ở memory).
4. Quy trình thêm trang/API/form mới (checklist).
5. Quy ước UI/Tailwind (component tái sử dụng, responsive, trạng thái loading/error/empty, accessibility).
6. Lệnh (`npm run dev/build/lint`), biến môi trường, Definition of Done, anti-patterns.

---

## 13. DOCKER, GIT

### 13.1 `backend/Dockerfile` (multi-stage)
- Stage build: `maven:3-eclipse-temurin-21` → cache dependency (`pom.xml` trước, `mvn dependency:go-offline`), `mvn -DskipTests package`.
- Stage run: `eclipse-temurin:21-jre-alpine`, tạo user non-root, `COPY` jar, `EXPOSE`, `HEALTHCHECK` (gọi `/actuator/health`), `ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]`. Thêm dependency `spring-boot-starter-actuator`.

### 13.2 `frontend/Dockerfile`
- Stage build: `node:20-alpine`, `npm ci`, `npm run build` (nhận `ARG VITE_API_BASE_URL`).
- Stage run: `nginx:alpine`, copy `dist` và `nginx.conf` (SPA fallback `try_files $uri /index.html`, gzip, cache static assets, security headers).

### 13.3 `docker-compose.yml` (full stack)
Services: `postgres` (image 16-alpine, volume, healthcheck `pg_isready`), `redis` (7-alpine, `--requirepass`, volume, healthcheck), `mailhog` (profile `dev`), `backend` (build `./backend`, `depends_on` với `condition: service_healthy`, env từ `.env`, `SPRING_PROFILES_ACTIVE`), `frontend` (build `./frontend`, port mapping). Dùng network riêng, named volumes, `restart: unless-stopped`, và mọi giá trị lấy từ `.env` (`${VAR:?message}` cho biến bắt buộc).

### 13.4 `docker-compose.dev.yml`
Chỉ chạy `postgres`, `redis`, `mailhog` (publish port ra host) để chạy backend/frontend bằng IDE.

### 13.5 `.env.example` (root)
Liệt kê **đủ** biến: `SPRING_PROFILES_ACTIVE`, `DB_*`, `POSTGRES_*`, `REDIS_*`, `JWT_SECRET`, `JWT_ISSUER`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `MAIL_*`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `CORS_ALLOWED_ORIGINS`, `COOKIE_DOMAIN`, `FRONTEND_BASE_URL`, `VITE_API_BASE_URL`. Có ghi chú cách sinh secret (`openssl rand -base64 64`).

### 13.6 `.gitignore`
- **Root**: `.env`, `.env.*` (trừ `.env.example`), `*.log`, `.DS_Store`, `.idea/`, `.vscode/` (giữ `extensions.json` nếu có), volume data của docker.
- **backend**: `target/`, `*.class`, `*.jar` (trừ wrapper), `.mvn/wrapper/maven-wrapper.jar` được giữ, `*.iml`, `.idea/`, `.classpath`, `.project`, `.settings/`, `HELP.md`, `application-local*.yml`, `*.pem`, `*.p12`, `*.jks`.
- **frontend**: `node_modules/`, `dist/`, `.env`, `.env.local`, `*.local`, `coverage/`, `.vite/`, `*.tsbuildinfo`, log npm/yarn/pnpm.
- Thêm `.dockerignore` cho backend và frontend.

---

## 14. LỘ TRÌNH TRIỂN KHAI THEO PHASE

Mỗi Phase kết thúc bằng: build pass + chạy được + tick checklist.

### Phase 0: Chuẩn bị nền
- [ ] Đọc `pom.xml`, xác định base package, bổ sung dependency (web, jpa, security, oauth2-client, validation, data-redis, mail, thymeleaf, lombok, postgresql, flyway-core + flyway-database-postgresql, jjwt, actuator, springdoc, test + testcontainers).
- [ ] Tạo cấu trúc thư mục/package rỗng, `.gitignore`, `.env.example`, `docker-compose.dev.yml`.
- [ ] Tạo 3 file yaml + các class `@ConfigurationProperties`.
- ✅ Chạy `docker compose -f docker-compose.dev.yml up -d` và app khởi động không lỗi (profile dev).

### Phase 1: Nền tảng dùng chung
- [ ] Toàn bộ `constant/`.
- [ ] `ApiResponse`, `PageResponse`, `FieldErrorResponse`.
- [ ] `AppException`, `GlobalExceptionHandler`.
- [ ] `BaseEntity`, `JpaAuditingConfig`, `AuditorAwareImpl`.
- ✅ Test nhanh: exception trả đúng JSON `{code, message, result}`.

### Phase 2: Entity, Repository, Migration
- [ ] `User`, `Role`, `Permission`, `TokenValidation`.
- [ ] Repository (kèm `@EntityGraph`, `JpaSpecificationExecutor` cho user).
- [ ] `V1__init_schema.sql` khớp entity (`ddl-auto: validate` pass).
- [ ] `DataInitializer` (permission, role, admin).
- ✅ Khởi động: DB có role/permission/admin; chạy lại không tạo trùng.

### Phase 3: Bảo mật lõi & JWT
- [ ] `JwtService`, `TokenBlacklistService` (Redis + DB + warm-up), `RedisConfig`.
- [ ] `JwtAuthenticationFilter`, `CustomAuthenticationEntryPoint`, `CustomAccessDeniedHandler`, `SecurityConfig`, CORS.
- [ ] `CurrentUserService`, cấu hình cookie refresh.
- ✅ Unit test JwtService (sinh/parse/hết hạn/sai chữ ký/sai type).

### Phase 4: Auth cơ bản
- [ ] `register`, `login`, `refresh` (rotation + reuse detection), `logout`, `logout-all`.
- [ ] `LoginAttemptService` (khoá tạm).
- [ ] Mapper, request/response, controller.
- ✅ Test luồng: register → login → gọi `/users/me` → refresh → logout → token cũ bị từ chối.

### Phase 5: Mail, OTP, Quên mật khẩu, Xác thực email
- [ ] `MailService` + template Thymeleaf, `AsyncConfig`.
- [ ] `OtpService`.
- [ ] `verify-email`, `resend-verification`, `forgot-password`, `verify-reset-otp`, `reset-password`.
- ✅ Kiểm tra bằng MailHog (`http://localhost:8025`): nhận mail, OTP đúng/sai/hết hạn/quá số lần/cooldown.

### Phase 6: Login Google
- [ ] Cấu hình `oauth2Login`, cookie-based authorization request repository.
- [ ] `OAuth2SuccessHandler` / `OAuth2FailureHandler`, logic tạo/liên kết user.
- ✅ Đăng nhập Google thật (dev) → redirect về FE với refresh cookie → `/auth/refresh` trả access token.

### Phase 7: API người dùng hiện tại
- [ ] `getMe`, `updateProfile`, `updateAvatar`, `changePassword`, `deactivateAccount`.
- ✅ Đổi mật khẩu → mọi token cũ bị vô hiệu.

### Phase 8: Admin (User, Role, Permission)
- [ ] `AdminUserService` + controller (search động, CRUD, status, roles, reset password, force logout, soft delete, restore, statistics).
- [ ] `RoleService`, `PermissionService` + controller.
- [ ] `@PreAuthorize` theo permission, các bảo vệ tự thân (không tự khoá/xoá, không gỡ ADMIN cuối).
- ✅ User thường gọi API admin → 403; MODERATOR chỉ làm được phần được cấp.

### Phase 9: Hoàn thiện backend
- [ ] springdoc (Swagger UI, mô tả security scheme Bearer), actuator health.
- [ ] Unit test service quan trọng (Auth, User, AdminUser, TokenBlacklist, Otp), integration test bằng Testcontainers cho luồng auth.
- [ ] Rà soát: không còn hard-code, không có logic trong controller, không log dữ liệu nhạy cảm.
- [ ] Viết `backend/skill.md`.
- ✅ `mvn clean verify` pass.

### Phase 10: Frontend nền tảng
- [ ] Quét qua toàn bộ thư mục frontend lại để xác nhận về kiến trúc và nắm tổng quan. Đồng thời check để add skill
- ✅ Silent refresh khi mở app hoạt động.

### Phase 11: Frontend auth & profile
- [ ] Login, Register, Verify email, Forgot/OTP/Reset, OAuth2 callback, Profile (4 tab).
- ✅ Chạy end-to-end với backend dev cho toàn bộ luồng ở mục 6.4.

### Phase 12: Frontend admin
- [ ] Dashboard, User list/detail/form, phân quyền hiển thị theo permission.
- [ ] Viết `frontend/skill.md`.
- ✅ `npm run build` và `npm run lint` pass.

### Phase 13: Docker hoá & tài liệu
- [ ] `backend/Dockerfile`, `frontend/Dockerfile`, `nginx.conf`, `.dockerignore`, `docker-compose.yml`.
- [ ] `.agent/skills/*`.
- [ ] `README.md`: kiến trúc, sơ đồ luồng token, hướng dẫn chạy dev/prod, cách lấy Google OAuth credentials (Authorized redirect URI đúng theo `redirect-uri` trong yaml), bảng biến môi trường, danh sách API.
- ✅ `docker compose up --build` chạy được toàn bộ; đăng nhập bằng tài khoản admin bootstrap; đăng nhập Google hoạt động.

---

## 15. CHECKLIST NGHIỆM THU CUỐI

**Convention**
- [ ] Có `application.yml`, `application-dev.yml`, `application-prod.yml`.
- [ ] Tìm kiếm trong code không còn: số magic về thời gian, chuỗi secret, URL, tên cookie/header, chuỗi role/permission, mã HTTP viết tay (`grep` thử các chuỗi như `"Bearer "`, `"ROLE_"`, `"ADMIN"`, `http://`, `localhost`, `900`, `604800`).
- [ ] Mọi response đều là `ApiResponse<T>`; lỗi đều qua `GlobalExceptionHandler`/EntryPoint/AccessDeniedHandler.
- [ ] Mỗi API có request/response riêng; entity không lộ ra ngoài.
- [ ] Controller không chứa logic; mapper viết tay.
- [ ] `BaseEntity` có đủ `id, createdAt, createdBy, status, modifiedAt, modifiedBy` với giá trị mặc định hợp lý.
- [ ] Có 4 entity: `User`, `Role`, `Permission`, `TokenValidation`.
- [ ] Có `backend/skill.md`, `frontend/skill.md`, `.gitignore` đầy đủ, Dockerfile & docker-compose đầy đủ.

**Bảo mật**
- [ ] Mật khẩu/OTP chỉ lưu dạng hash; secret không có trong repo.
- [ ] Refresh token trong HttpOnly cookie; rotation + reuse detection hoạt động.
- [ ] Logout, đổi mật khẩu, khoá user, force-logout đều vô hiệu hoá token thực sự.
- [ ] Quên mật khẩu không cho phép dò email tồn tại; OTP có giới hạn thử và cooldown.
- [ ] Rate-limit/khoá đăng nhập hoạt động; CORS chỉ cho origin cấu hình.
- [ ] Prod: `cookie.secure=true`, Swagger tắt, không có default secret.

**Chức năng**
- [ ] Toàn bộ 38 API ở mục 7 hoạt động đúng phân quyền.
- [ ] Frontend hoàn thiện các trang ở mục 11, luồng auth ổn định (không bị vòng lặp refresh).
- [ ] `docker compose up --build` chạy trọn vẹn.

---

## 16. NGOÀI PHẠM VI (KHÔNG LÀM TRỪ KHI ĐƯỢC YÊU CẦU)
Upload file avatar (chỉ lưu URL), MFA/2FA, đăng nhập Facebook/GitHub, đa tenant, audit log riêng, i18n đầy đủ. Kiến trúc hiện tại cho phép mở rộng các mục này về sau (ví dụ thêm giá trị vào `AuthProvider`).
