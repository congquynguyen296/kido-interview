---
name: backend-conventions
description: Quy ước code, kiến trúc và checklist khi phát triển backend auth-service (Spring Boot). Dùng khi thêm/sửa API, entity, service, cấu hình.
---

# 1. Tổng quan dự án và tech stack
- **Backend framework**: Spring Boot 3.x, Java 21.
- **Database**: PostgreSQL 16 + Flyway (migration).
- **Cache/Session**: Redis 7.
- **Security**: Spring Security 6, JWT, OAuth2 Client.
- **Architecture**: MVC REST API (Controller -> Service -> Repository).

# 2. Cấu trúc thư mục (`com.kido.corporation.auth`)
- `config/`: Cấu hình Spring (Security, JWT, Async, OpenAPI, DataInitializer...).
- `constant/`: Hằng số (HTTPCode, PredefinedRole, RedisKey...). TUYỆT ĐỐI KHÔNG hard-code.
- `controller/`: Request -> `@Valid` -> Service -> `ApiResponse`.
- `dto/request/` và `dto/response/`: DTOs cho từng API.
- `entity/`: Kế thừa `BaseEntity`.
- `exception/`: Xử lý ngoại lệ (`AppException`, `GlobalExceptionHandler`).
- `repository/`: Spring Data JPA (cùng `JpaSpecificationExecutor`).
- `service/` & `service/impl/`: Tầng nghiệp vụ, gọi Repository/Mapper.
- `mapper/`: Viết tay, dùng `@Component`.

**Luật phụ thuộc:** Controller -> Service -> Repository. Mapper chỉ được gọi từ Service. Không import Repository hay Entity vào Controller.

# 3. Quy ước chung
- Không hard-code thời gian, HTTP code, URL, hay JWT configs. Mọi thứ được định nghĩa trong `application.yml` hoặc package `constant/`.
- Mọi Response từ Controller phải bọc trong `ApiResponse<T>`.
- Mỗi API có Request/Response riêng, không dùng trực tiếp Entity.
- Entity phải extend `BaseEntity`, khai báo `@SuperBuilder`, không dùng `@Data`.
- Dùng `@FieldDefaults(level = AccessLevel.PRIVATE)` trong DTO.

# 4. Quy trình thêm API mới
1. Thêm constant `HTTPCode` nếu cần mã lỗi mới.
2. Thêm Request/Response DTO.
3. Cập nhật Service (interface và impl).
4. Thêm method vào Controller, cấu hình `@PreAuthorize("hasAuthority('X')")`.
5. Đăng ký quyền/Role ở `PredefinedPermission`, `PredefinedRole` và `DataInitializer`.
6. Cấu hình YAML (nếu có thêm biến mới).
7. Flyway migration (nếu có Entity mới).
8. Unit test.

# 5. Quy trình thêm Entity/Migration
1. Tạo Entity kế thừa `BaseEntity` (id, createdAt, createdBy, modifiedAt, modifiedBy, status).
2. Viết file migration vào `src/main/resources/db/migration/` (theo chuẩn `V{x}__name.sql`).
3. Khai báo Repository (Spring Data JPA).

# 6. Quy tắc bảo mật
- **Không log** dữ liệu nhạy cảm: Password, JWT, OTP, thông tin cá nhân (PII).
- **Token**: Access Token ở header (`Bearer`), Refresh Token ở cookie (`HttpOnly`).
- Nếu detect token reuse, sẽ invalidate tất cả phiên người dùng.

# 7. Hướng dẫn Test và Chạy dự án
- Chạy môi trường DB dev: `docker compose -f docker-compose.dev.yml up -d`
- Chạy backend (Terminal): `./mvnw clean spring-boot:run -Dspring-boot.run.profiles=dev`
- Test (Mockito + Testcontainers): Chạy qua IDE hoặc `mvn test`.

# 8. Lệnh và Biến Môi Trường
- Lệnh: `mvn clean verify` (build test), `mvn compile` (biên dịch).
- Môi trường (cần cho `.env`): DB_HOST, DB_USER, DB_PASS, REDIS_HOST, JWT_SECRET, GOOGLE_CLIENT_ID...

# 9. KHÔNG ĐƯỢC LÀM (Anti-patterns)
- Đưa logic vào Controller.
- Dùng Entity trả thẳng về FE (phải map sang Response DTO).
- MapStruct/công cụ gen Mapper tự động (hiện tại bắt buộc viết tay Mapper).
- N+1 query: Cần dùng `@EntityGraph` hoặc `JOIN FETCH` trong Repository.
- Bỏ qua exception chung, ném exception trắng ra FE. Lỗi phải ném `AppException(HTTPCode.XYZ)`.
