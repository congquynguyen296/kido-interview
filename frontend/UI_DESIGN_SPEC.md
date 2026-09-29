# YÊU CẦU THIẾT KẾ GIAO DIỆN (FRONTEND FIRST, DÙNG MOCK DATA)

> Tài liệu dành cho AI Agent thực thi. Đọc **toàn bộ** trước khi code. Đây là **bước 1/3** của dự án:
>
> 1. **(File này)** Dựng toàn bộ giao diện frontend, chạy được độc lập bằng **mock data**.
> 2. Chạy `IMPLEMENTATION_PLAN.md` để dựng backend (và các phần còn lại của plan).
> 3. **Connect**: tắt mock, nối frontend với backend thật (xem mục 12).
>
> Vì vậy toàn bộ code frontend viết ở bước này phải **giữ nguyên hợp đồng API** (endpoint, kiểu dữ liệu, mã lỗi) đúng như `IMPLEMENTATION_PLAN.md` để bước connect chỉ cần đổi một cờ cấu hình.

---

## 0. QUY TẮC LÀM VIỆC CHO AI

1. Thư mục làm việc: `frontend/` (tạo mới bằng Vite React + TypeScript nếu chưa có). Không đụng vào `backend/`.
2. Stack: React 18, TypeScript (strict, cấm `any`), Vite, **TailwindCSS**, React Router v6, TanStack Query, Axios, React Hook Form + Zod, Zustand, lucide-react, react-hot-toast.
3. **Không dùng thư viện UI dựng sẵn** (MUI, Ant, shadcn, Chakra…). Tự viết component bằng Tailwind theo design system ở mục 2 để đồng bộ với mẫu form login.
4. **Không hard-code** route path, endpoint, mã lỗi, tên role/permission, query key, message, thời gian (đếm ngược OTP, latency mock, debounce…), kích thước trang. Tất cả ở `src/constants/` hoặc `.env`.
5. Không dùng ảnh/icon từ URL bên ngoài (mẫu login đang dùng `raw.githubusercontent.com`): mọi logo/icon là **inline SVG component** hoặc `lucide-react`.
6. **Responsive bắt buộc** cho mọi màn hình (mục 3). Không có scroll ngang ở body ở bất kỳ độ rộng nào từ 320px.
7. Mọi màn hình phải có đủ trạng thái: **loading (skeleton/spinner), empty, error (có nút thử lại), success**.
8. Mọi input có `<label>` (dùng `sr-only` nếu thiết kế chỉ hiện placeholder), `aria-invalid`, `aria-describedby` cho lỗi, focus ring nhìn thấy được, điều hướng bằng bàn phím được.
9. Sau mỗi Phase (mục 13): `npm run build` và `npm run lint` phải pass.
10. Toàn bộ UI dùng **tiếng Việt** (chuỗi nằm trong `constants/messages.ts` để sau này đa ngôn ngữ dễ dàng).

---

## 1. PHÂN TÍCH MẪU GIAO DIỆN GỐC (BẮT BUỘC GIỮ PHONG CÁCH)

Mẫu login gốc:

```jsx
<div className="bg-white text-gray-500 max-w-96 mx-4 md:p-6 p-4 text-left text-sm rounded-xl shadow-[0px_0px_10px_0px] shadow-black/10">
  <h2 className="text-2xl font-semibold mb-6 text-center text-gray-800">Welcome back</h2>
  <input className="w-full bg-transparent border my-3 border-gray-500/30 outline-none rounded-full py-2.5 px-4" .../>
  <button className="w-full mb-3 bg-indigo-500 py-2.5 rounded-full text-white">Log in</button>
  ...
```

Đặc trưng phong cách cần bảo toàn ở **toàn bộ** ứng dụng (kể cả trang admin):

| Đặc trưng | Giá trị |
|---|---|
| Card | nền trắng, `rounded-xl`, bóng nhẹ `shadow-[0px_0px_10px_0px] shadow-black/10`, padding `p-4 md:p-6` |
| Chữ | mặc định `text-sm text-gray-500`, tiêu đề `font-semibold text-gray-800` |
| Input | **dạng viên thuốc** `rounded-full`, `border border-gray-500/30`, `py-2.5 px-4`, `outline-none`, `bg-transparent` |
| Nút chính | `bg-indigo-500 text-white rounded-full py-2.5` |
| Nút phụ | trắng, `border border-gray-500/30`, `text-gray-800`, `rounded-full` |
| Nút màu đen (social) | `bg-black text-white rounded-full` |
| Link | `text-blue-600 underline` (hoặc `text-blue-500`) |
| Tổng thể | phẳng, tối giản, nhiều khoảng trắng, bo tròn lớn, không viền đậm, không gradient nặng |

**Các lỗi/thiếu sót trong mẫu cần sửa khi triển khai** (không thay đổi tinh thần thiết kế):
- Nút Google trong mẫu ghi nhầm chữ "Log in with Apple" → phải là "Đăng nhập bằng Google".
- **Bỏ nút Apple** (dự án chỉ hỗ trợ Google, xem plan mục 16).
- Thiếu `<label>`, thiếu focus state, thiếu hiển thị lỗi, thiếu hiện/ẩn mật khẩu, thiếu `disabled`/loading trên nút → bổ sung theo mục 2.
- `max-w-96` cố định: chấp nhận cho form ngắn (login, forgot…), form dài (register) dùng `max-w-md`.

---

## 2. DESIGN SYSTEM

### 2.1 Design tokens (khai báo trong `tailwind.config` `theme.extend` hoặc dùng class trực tiếp, nhưng **chỉ dùng đúng bộ này**)

| Token | Giá trị Tailwind |
|---|---|
| Primary | `indigo-500` (hover `indigo-600`, active `indigo-700`, nền nhạt `indigo-50`, chữ nhạt `indigo-600`) |
| Text chính | `gray-800` |
| Text phụ | `gray-500` |
| Text mờ / placeholder | `gray-400` |
| Viền | `gray-500/30` (viền input/card phụ), `gray-200` (đường kẻ bảng) |
| Nền trang | `gray-50` |
| Nền card | `white` |
| Link | `blue-600` |
| Thành công | `emerald-500` (nền `emerald-50`, chữ `emerald-700`) |
| Cảnh báo | `amber-500` (nền `amber-50`, chữ `amber-700`) |
| Lỗi / nguy hiểm | `red-500` (nền `red-50`, chữ `red-600`, hover `red-600`) |
| Bo góc | card/modal `rounded-xl`; input, nút, badge, tab `rounded-full`; ô nhỏ (avatar vuông, ảnh) `rounded-lg` |
| Bóng | card `shadow-[0px_0px_10px_0px] shadow-black/10`; dropdown/modal `shadow-[0px_4px_24px_0px] shadow-black/15` |
| Font | `font-sans` mặc định của Tailwind (có thể thêm Inter qua `@fontsource/inter`, cài local, **không** dùng Google Fonts CDN) |
| Cỡ chữ | body `text-sm`; tiêu đề trang `text-2xl font-semibold`; tiêu đề card `text-lg font-semibold`; chú thích `text-xs` |

### 2.2 Bộ component (`src/components/ui/`) và đặc tả

Mọi component nhận `className` để mở rộng, forward ref, hỗ trợ `disabled`.

| Component | Đặc tả |
|---|---|
| `Button` | variants: `primary` (indigo), `secondary` (viền), `dark` (đen), `danger` (đỏ), `ghost` (chỉ hover xám nhạt), `link`. Size `md` (`py-2.5`, `min-h-11`) và `sm` (`py-1.5 text-xs`). Props `loading` (spinner + disable), `fullWidth`, `leftIcon`. Focus: `focus-visible:ring-2 ring-indigo-500/40 ring-offset-2`. |
| `Input` | pill, có `label` (sr-only hoặc hiển thị qua prop `showLabel`), `error`, `hint`, `leftIcon`. Focus: `focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20`. Lỗi: `border-red-500`, text lỗi `text-xs text-red-500 mt-1 px-4`. |
| `PasswordInput` | `Input` + nút mắt (lucide `Eye`/`EyeOff`) đặt `absolute right-4`, `aria-label` đổi theo trạng thái. |
| `PasswordStrengthMeter` | 4 thanh nhỏ dưới ô mật khẩu (đỏ → cam → vàng → xanh) + nhãn "Yếu/Trung bình/Khá/Mạnh". |
| `OtpInput` | 6 ô vuông bo `rounded-lg`, tự nhảy ô, hỗ trợ dán cả chuỗi, backspace lùi ô, `inputMode="numeric"`, `autoComplete="one-time-code"`. |
| `Select` | giống `Input` (pill), dùng `<select>` native có mũi tên riêng (`ChevronDown`) để tốt trên mobile. |
| `Textarea`, `Checkbox`, `Switch`, `Radio` | cùng ngôn ngữ (viền `gray-500/30`, tick màu indigo). |
| `Avatar` | ảnh tròn; không có ảnh → hiện chữ cái đầu trên nền `indigo-50` chữ `indigo-600`. Size `sm/md/lg/xl`. |
| `Badge` | pill nhỏ `text-xs px-2.5 py-0.5`; tone: `gray`, `indigo`, `emerald`, `amber`, `red`. Có map sẵn cho `EntityStatus`, role, `AuthProvider`. |
| `Card` | như mục 1; có `CardHeader` (tiêu đề + action) và `CardBody`. |
| `Modal` | overlay `bg-black/40`, khoá scroll nền, đóng bằng Esc/click nền, focus trap. Desktop: giữa màn hình, `max-w-lg`, `rounded-xl`. **Mobile (<sm): dạng bottom sheet** (bám đáy, bo góc trên, `max-h-[90vh]` cuộn nội dung). |
| `ConfirmDialog` | dựa trên `Modal`, 2 nút; biến thể nguy hiểm (nút đỏ); tuỳ chọn bắt gõ lại từ khoá xác nhận (dùng cho xoá). |
| `Dropdown / Menu` | menu kebab và user menu, đóng khi click ngoài/Esc, điều hướng phím mũi tên. |
| `Tabs` | pill tabs; mobile cuộn ngang (`overflow-x-auto`, ẩn scrollbar) — chỉ vùng tabs được cuộn, không phải body. |
| `DataTable` | dùng cho desktop (`md` trở lên); có cột sort, hàng loading skeleton, empty state. **Dưới `md` tự chuyển sang danh sách card** (mỗi hàng là một card: dòng đầu tên + badge, các dòng sau cặp nhãn–giá trị, nút hành động dưới cùng). Cung cấp render-prop `renderMobileCard`. |
| `Pagination` | nút tròn/pill, hiển thị "Trang x/y", chọn kích cỡ trang; mobile rút gọn còn Trước/Sau + "x/y". |
| `SearchInput` | `Input` có icon kính lúp và nút xoá, debounce (giá trị lấy từ constants). |
| `Skeleton` | `animate-pulse bg-gray-200 rounded-*`, dựng sẵn `SkeletonText`, `SkeletonCard`, `SkeletonTableRow`. |
| `EmptyState` | icon lớn mờ + tiêu đề + mô tả + nút hành động. |
| `ErrorState` | icon cảnh báo + thông điệp + nút "Thử lại". |
| `Alert` | khối thông báo inline (info/success/warning/error), `rounded-xl`, có icon. |
| `Spinner`, `Toast` | toast bằng react-hot-toast tuỳ biến: `rounded-xl`, bóng chuẩn, icon theo tone, hiển thị trên cùng giữa (mobile) / phải trên (desktop). |
| `GoogleIcon`, `Logo` | inline SVG; `Logo` = ô vuông `rounded-lg bg-indigo-500` chứa ký hiệu khoá/khiên (lucide `ShieldCheck` trắng) + tên app từ `VITE_APP_NAME`. |
| `StatCard`, `SimpleBarChart`, `DonutChart` | cho dashboard, tự vẽ bằng SVG/div (không thêm thư viện chart). |

### 2.3 Layout chung (`src/components/layout/`)

- **`AuthLayout`**: nền `bg-gray-50`, nội dung căn giữa cả hai chiều (`min-h-dvh grid place-items-center px-4 py-8`), `Logo` nhỏ phía trên card, footer nhỏ `text-xs text-gray-400`. Card theo mục 1.
- **`MainLayout`** (người dùng thường): header cố định trên, nền trắng, viền dưới `gray-500/20`; trái `Logo`; phải `Avatar` + menu người dùng (Hồ sơ, Trang quản trị nếu có quyền, Đăng xuất). Nội dung `max-w-5xl mx-auto px-4 py-6 md:py-10`.
- **`AdminLayout`**: sidebar trái `w-64` (desktop ≥`lg`), mục: Dashboard, Người dùng, Vai trò, Quyền hạn, (cuối) Về trang cá nhân, Đăng xuất; mục đang chọn là pill `bg-indigo-50 text-indigo-600`. **Dưới `lg`: sidebar là drawer** trượt từ trái, mở bằng nút hamburger ở header, có overlay, đóng khi chọn mục/Esc. Header admin có breadcrumb rút gọn, `Avatar` menu. Mục trong sidebar **chỉ hiển thị nếu user có permission tương ứng**.

---

## 3. QUY TẮC RESPONSIVE

Mobile-first: viết style cho mobile trước, mở rộng bằng `sm:`, `md:`, `lg:`, `xl:`.

| Breakpoint | Hành vi |
|---|---|
| `<640` (mobile) | 1 cột; card auth chiếm `mx-4`; modal thành bottom sheet; bảng thành danh sách card; sidebar admin thành drawer; bộ lọc bảng thu gọn trong nút "Bộ lọc" mở bottom sheet; hàng nút hành động xếp dọc full-width |
| `640–767` (sm) | Form 1 cột rộng hơn; lưới thống kê 2 cột |
| `768–1023` (md) | Bảng hiển thị dạng bảng thật; form hồ sơ 2 cột; padding card `p-6` |
| `≥1024` (lg) | Hiện sidebar cố định; layout 2 cột ở trang chi tiết (thông tin chính + cột phụ); lưới thống kê 4 cột |
| `≥1280` (xl) | Giới hạn `max-w-7xl`, không kéo giãn vô hạn |

Yêu cầu kiểm thử thủ công cho **mỗi màn hình** ở các độ rộng: **320, 360, 390, 768, 1024, 1440**.
- Vùng chạm tối thiểu `44×44px` (dùng `min-h-11`), không đặt hai nút nhỏ sát nhau.
- Dùng `min-h-dvh` thay `100vh` (thanh địa chỉ mobile). Tôn trọng `safe-area-inset` cho header cố định/bottom sheet.
- Input trên mobile `text-base` (≥16px) để iOS không tự phóng to khi focus (`text-base sm:text-sm`).
- Bảng rộng không được làm body cuộn ngang: hoặc chuyển thành card (mặc định) hoặc bọc `overflow-x-auto` **bên trong** card.
- Văn bản dài (email, tên) dùng `truncate`/`break-words` và `min-w-0` trong flex để không vỡ layout.

---

## 4. CẤU TRÚC THƯ MỤC FRONTEND (khớp với plan để bước connect không phải di chuyển file)

```
frontend/
├── .env.example
├── src/
│   ├── app/                 App.tsx, router.tsx, providers.tsx
│   ├── constants/           routes.ts, apiEndpoints.ts, httpCode.ts, roles.ts, permissions.ts,
│   │                        entityStatus.ts, authProvider.ts, queryKeys.ts, storageKeys.ts,
│   │                        messages.ts, config.ts   (đọc import.meta.env, có giá trị mặc định an toàn)
│   ├── types/               api.ts, auth.ts, user.ts, role.ts, permission.ts, admin.ts
│   ├── schemas/             zod schema từng form
│   ├── store/               authStore.ts
│   ├── api/
│   │   ├── contracts.ts     interface cho mọi API (chữ ký hàm)
│   │   ├── ApiError.ts      lớp lỗi chuẩn (code, message, fieldErrors)
│   │   ├── axiosClient.ts   axios thật + interceptor (viết đầy đủ, dùng ở bước connect)
│   │   ├── http/            authApi.ts, userApi.ts, adminUserApi.ts, adminRoleApi.ts, adminPermissionApi.ts
│   │   ├── mock/            bản mock cùng chữ ký (mục 9)
│   │   └── index.ts         export bản mock hoặc http tuỳ VITE_USE_MOCK
│   ├── mocks/               db.ts (state in-memory), seed/*.ts (mock data), utils.ts (delay, id, paginate)
│   ├── hooks/               useAuth, usePermission, useDebounce, useCountdown, useMediaQuery, hook react-query
│   ├── components/
│   │   ├── ui/  layout/  guards/
│   ├── pages/
│   │   ├── auth/  profile/  admin/  error/
│   └── utils/
├── skill.md
```

---

## 5. BIẾN MÔI TRƯỜNG (`.env.example`)

```
VITE_APP_NAME=Auth Service
VITE_API_BASE_URL=http://localhost:8080
VITE_USE_MOCK=true
VITE_MOCK_LATENCY_MIN_MS=300
VITE_MOCK_LATENCY_MAX_MS=800
```
`constants/config.ts` đọc các biến này; các hằng số giao diện khác cũng nằm ở đây: `OTP_LENGTH=6`, `OTP_RESEND_SECONDS=60`, `PASSWORD_MIN_LENGTH=8`, `SEARCH_DEBOUNCE_MS=400`, `DEFAULT_PAGE_SIZE=10`, `PAGE_SIZE_OPTIONS=[10,20,50]`, `TOAST_DURATION_MS`, `ACCESS_TOKEN_REFRESH_SKEW_MS`.

---

## 6. ROUTES

Toàn bộ nằm trong `constants/routes.ts`.

| Route | Màn hình | Layout | Guard |
|---|---|---|---|
| `/login` | Đăng nhập | Auth | Guest |
| `/register` | Đăng ký | Auth | Guest |
| `/verify-email` | Xác thực email | Auth | Public |
| `/forgot-password` | Quên mật khẩu (bước 1) | Auth | Guest |
| `/forgot-password/verify` | Nhập OTP (bước 2) | Auth | Guest |
| `/reset-password` | Đặt mật khẩu mới (bước 3) | Auth | Guest |
| `/oauth2/callback` | Xử lý kết quả đăng nhập Google | Auth | Public |
| `/profile` | Hồ sơ cá nhân (4 tab) | Main | Protected |
| `/admin` | Dashboard | Admin | Protected + `USER_READ` |
| `/admin/users` | Danh sách người dùng | Admin | `USER_READ` |
| `/admin/users/:id` | Chi tiết người dùng | Admin | `USER_READ` |
| `/admin/roles` | Quản lý vai trò | Admin | `ROLE_READ` |
| `/admin/permissions` | Quản lý quyền hạn | Admin | `PERMISSION_READ` |
| `/403` `/404` | Lỗi | Auth/Main | — |
| `/` | Chuyển hướng: chưa đăng nhập → `/login`; user thường → `/profile`; có quyền admin → `/admin` | — | — |

---

## 7. ĐẶC TẢ TỪNG MÀN HÌNH

Mỗi màn hình bên dưới ghi rõ: bố cục, thành phần, validate, trạng thái, hành vi mock, và **API tương ứng trong plan** (để viết `api/http/*` đúng và để bước connect không sai lệch).

### 7.1 Đăng nhập `/login`
- **Bố cục**: card `max-w-96` trong `AuthLayout`. Tiêu đề "Chào mừng trở lại" (`text-2xl font-semibold text-center`), 2 input pill (email, mật khẩu có nút mắt), link "Quên mật khẩu?" căn phải, nút chính "Đăng nhập", dòng "Chưa có tài khoản? **Đăng ký**", vạch phân cách "hoặc" (đường kẻ `gray-500/20` + chữ nhỏ), nút Google (trắng, viền, `GoogleIcon` + "Đăng nhập bằng Google").
- **Validate (zod)**: email hợp lệ; mật khẩu không rỗng. Lỗi hiển thị dưới từng ô.
- **Trạng thái**: nút loading khi submit; lỗi chung (`INVALID_CREDENTIALS`) hiện `Alert` đỏ trên form; `ACCOUNT_LOCKED` → Alert cam có thông báo thời gian khoá; `ACCOUNT_DISABLED` → Alert đỏ; `EMAIL_NOT_VERIFIED` → Alert cam kèm nút "Gửi lại email xác thực".
- **Sau khi thành công**: lưu access token vào store (memory), lấy `user`, chuyển hướng về trang đích đã nhớ (nếu có) hoặc theo quy tắc route `/`.
- **API**: `POST /api/v1/auth/login` → `LoginResponse`; Google: điều hướng `window.location.href = {API_BASE}/oauth2/authorization/google`.

### 7.2 Đăng ký `/register`
- **Bố cục**: card `max-w-md`. Trường: Họ tên, Email, Tên đăng nhập (tuỳ chọn), Số điện thoại (tuỳ chọn), Mật khẩu (+ `PasswordStrengthMeter`), Xác nhận mật khẩu; checkbox "Tôi đồng ý với Điều khoản" (bắt buộc, chỉ ở FE). Trên `md`, Họ tên/Tên đăng nhập và Mật khẩu/Xác nhận xếp 2 cột; mobile 1 cột. Nút "Tạo tài khoản", link "Đã có tài khoản? Đăng nhập", nút Google.
- **Validate**: email; mật khẩu ≥ `PASSWORD_MIN_LENGTH`, có hoa/thường/số/ký tự đặc biệt; xác nhận khớp; SĐT đúng định dạng (regex đặt ở constants).
- **Lỗi từ server**: `EMAIL_ALREADY_EXISTS` / `USERNAME_ALREADY_EXISTS` → gắn vào đúng ô; `VALIDATION_FAILED` → map `fieldErrors` vào form.
- **Thành công**: chuyển sang trạng thái "Kiểm tra hộp thư của bạn" ngay trong card (icon phong bì, hiển thị email, nút "Gửi lại email" có cooldown, link "Về đăng nhập"); nếu `emailVerificationRequired=false` thì hiện thông báo thành công + nút "Đăng nhập ngay".
- **API**: `POST /api/v1/auth/register` → `RegisterResponse`; `POST /api/v1/auth/resend-verification`.

### 7.3 Xác thực email `/verify-email?email=&token=`
- 3 trạng thái trong card: **đang xác thực** (spinner), **thành công** (icon tích xanh + nút "Đăng nhập"), **thất bại/hết hạn** (icon đỏ, ô nhập email + nút "Gửi lại email xác thực").
- Tự gọi API khi vào trang nếu có đủ `email`, `token`; thiếu → trạng thái thất bại.
- **API**: `POST /api/v1/auth/verify-email`, `POST /api/v1/auth/resend-verification`.

### 7.4 Quên mật khẩu — bước 1 `/forgot-password`
- Card `max-w-96`: tiêu đề "Quên mật khẩu?", mô tả ngắn, 1 ô email, nút "Gửi mã xác nhận", link "Quay lại đăng nhập". Có **thanh tiến trình 3 bước** nhỏ (3 chấm/đoạn, bước hiện tại màu indigo).
- Thành công: **luôn** điều hướng sang bước 2 dù email có tồn tại hay không (đúng với plan, không lộ email); truyền `email` qua state/router.
- **API**: `POST /api/v1/auth/forgot-password` → `ForgotPasswordResponse` (`resendAfterSeconds`).

### 7.5 Quên mật khẩu — bước 2 `/forgot-password/verify`
- Hiển thị email đã gửi (che một phần: `us***@example.com`), `OtpInput` 6 ô, nút "Xác nhận", dòng "Chưa nhận được mã? **Gửi lại**" với đếm ngược `OTP_RESEND_SECONDS` (`useCountdown`), link "Đổi email".
- Trạng thái lỗi: OTP sai (ô đổi viền đỏ + rung nhẹ, hiển thị số lần còn lại nếu mock trả về), hết hạn (`OTP_EXPIRED`), quá số lần (`OTP_TOO_MANY_ATTEMPTS` → khoá nút, hướng dẫn quay lại bước 1), gửi lại quá sớm (`OTP_RESEND_TOO_SOON`).
- Không có `email` trong state (vào thẳng URL) → chuyển về bước 1.
- **API**: `POST /api/v1/auth/verify-reset-otp` → `VerifyResetOtpResponse` (`resetToken`, `expiresIn`); gửi lại dùng lại `forgot-password`.

### 7.6 Quên mật khẩu — bước 3 `/reset-password`
- Mật khẩu mới (+ meter), xác nhận mật khẩu, nút "Đặt lại mật khẩu". Giữ `resetToken` trong state điều hướng/memory (không lưu localStorage). Thiếu token → về bước 1. Token hết hạn (`RESET_TOKEN_INVALID`) → Alert + link về bước 1.
- Thành công: màn hình kết quả trong card (icon tích xanh, "Đặt lại mật khẩu thành công", nút "Đăng nhập").
- **API**: `POST /api/v1/auth/reset-password` → `ResetPasswordResponse`.

### 7.7 Callback Google `/oauth2/callback`
- Card nhỏ hiển thị spinner "Đang hoàn tất đăng nhập…". Gọi silent refresh rồi `GET users/me`, thành công → điều hướng như sau đăng nhập; thất bại → trạng thái lỗi + nút "Về trang đăng nhập". Xử lý query `?error=oauth2` (từ `frontend-failure-redirect-uri` của plan).
- **Mock**: nút Google ở mock mode **không** rời trang; hiển thị một `Modal` "Chọn tài khoản Google (mô phỏng)" với 2 tài khoản mẫu, chọn xong điều hướng tới `/oauth2/callback` và tạo phiên mock.
- **API**: `POST /api/v1/auth/refresh` (cookie), `GET /api/v1/users/me`.

### 7.8 Hồ sơ cá nhân `/profile` (MainLayout)
Đầu trang: card tóm tắt (Avatar `xl`, tên, email, badge role, badge provider, badge "Đã xác thực"/"Chưa xác thực"). Bên dưới là `Tabs`:

**Tab 1 — Thông tin cá nhân**: form 2 cột (`md`), 1 cột mobile: Họ tên, Tên đăng nhập, Số điện thoại, Ngày sinh (`type=date`), Giới tính (Select), Email (chỉ đọc, có icon khoá). Khối "Ảnh đại diện": xem trước Avatar + ô nhập URL ảnh + nút "Cập nhật ảnh" (chỉ lưu URL, đúng plan). Nút "Lưu thay đổi" chỉ bật khi form `dirty`; nút "Hoàn tác".
- API: `GET /users/me`, `PUT /users/me` (`UpdateProfileRequest/Response`), `PATCH /users/me/avatar`.

**Tab 2 — Đổi mật khẩu**: Mật khẩu hiện tại, mật khẩu mới (+ meter), xác nhận. Nếu `authProvider = GOOGLE` (không có mật khẩu): hiện `Alert` info "Tài khoản đăng nhập bằng Google không có mật khẩu" và ẩn form. Thành công: toast, sau đó **đăng xuất và chuyển về `/login`** (đúng plan: đổi mật khẩu thu hồi mọi phiên).
- API: `PATCH /users/me/password`.

**Tab 3 — Bảo mật & phiên**: mô tả ngắn, nút "Đăng xuất khỏi tất cả thiết bị" (`ConfirmDialog`), nút "Đăng xuất thiết bị này".
- API: `POST /auth/logout-all`, `POST /auth/logout`.

**Tab 4 — Vùng nguy hiểm**: khối viền đỏ nhạt (`border-red-200 bg-red-50/40`), nút "Vô hiệu hoá tài khoản" → `ConfirmDialog` nguy hiểm, có ô nhập mật khẩu (nếu LOCAL), thành công → đăng xuất về `/login`.
- API: `DELETE /users/me` (`DeactivateAccountRequest`).

### 7.9 Admin — Dashboard `/admin`
- Hàng `StatCard` (1/2/4 cột theo breakpoint): Tổng người dùng, Đang hoạt động, Bị khoá, Chưa xác thực. Mỗi card: icon tròn nền nhạt, số lớn `text-2xl font-semibold`, nhãn `text-gray-500`.
- Card "Đăng ký mới": `SimpleBarChart` với chuyển đổi **7 ngày / 30 ngày** (pill toggle). Mobile: biểu đồ cuộn ngang trong card khi 30 cột.
- Card "Theo trạng thái" (`DonutChart` + chú giải) và "Theo phương thức đăng nhập" (2 thanh ngang LOCAL vs GOOGLE).
- Card "Người dùng mới nhất" (5 dòng, link "Xem tất cả").
- Loading: skeleton cho từng card. Lỗi: `ErrorState` trong card.
- **API**: `GET /admin/users/statistics` → `AdminUserStatisticsResponse`; danh sách mới nhất dùng `GET /admin/users?size=5&sort=createdAt,desc`.

### 7.10 Admin — Danh sách người dùng `/admin/users`
- **Thanh công cụ**: `SearchInput` (keyword), bộ lọc: Trạng thái, Vai trò, Phương thức (LOCAL/GOOGLE), Xác thực email (Tất cả/Đã/Chưa), Từ ngày–Đến ngày; nút "Xoá bộ lọc"; nút chính "+ Thêm người dùng" (chỉ khi có `USER_CREATE`). Desktop: các bộ lọc nằm 1–2 hàng. **Mobile: chỉ còn ô tìm kiếm + nút "Bộ lọc" (hiện số bộ lọc đang bật) mở bottom sheet**; nút thêm thành nút tròn nổi (FAB) góc dưới phải.
- **Bảng (≥md)**: cột: Người dùng (Avatar + tên + email), Vai trò (badges), Trạng thái (badge), Phương thức, Xác thực (icon), Ngày tạo, Thao tác (kebab: Xem, Sửa, Đổi trạng thái, Gán vai trò, Reset mật khẩu, Buộc đăng xuất, Xoá/Khôi phục). Cột sort được: Người dùng, Ngày tạo, Trạng thái. Người dùng đã `DELETED` hiển thị mờ + badge xám, menu chỉ còn "Khôi phục".
- **Card (<md)**: theo đặc tả `DataTable`.
- Phân trang phía server; **trạng thái bộ lọc/sort/trang được đồng bộ lên query string URL** để refresh/chia sẻ link không mất.
- Hành động trong kebab mở đúng modal/confirm ở 7.12; sau khi thành công `invalidateQueries` và toast.
- Ẩn/vô hiệu mục menu theo permission (`USER_UPDATE`, `USER_MANAGE_STATUS`, `USER_ASSIGN_ROLE`, `SESSION_REVOKE`, `USER_DELETE`). Không cho tác động lên **chính mình** với các thao tác khoá/xoá (mục bị disable kèm tooltip lý do).
- **API**: `GET /admin/users` (`AdminUserSearchRequest` qua query) → `PageResponse<AdminUserListItemResponse>`.

### 7.11 Admin — Chi tiết người dùng `/admin/users/:id`
- Breadcrumb "Người dùng / {tên}". Header card: Avatar `xl`, tên, email, hàng badge (status, role, provider, xác thực) và nhóm nút hành động (desktop: hàng nút; mobile: nút "Thao tác" mở bottom sheet liệt kê).
- Desktop `lg`: 2 cột — trái (2/3): tab **Thông tin** (dạng danh sách nhãn–giá trị: họ tên, username, SĐT, ngày sinh, giới tính, phương thức, providerId); tab **Vai trò & quyền** (danh sách role, mỗi role mở rộng ra permission dạng chip; dòng "Tổng hợp quyền hiệu lực"); phải (1/3): card "Hoạt động & Audit" (createdAt/createdBy, modifiedAt/modifiedBy, lastLoginAt, passwordChangedAt). Mobile: xếp dọc.
- Không tìm thấy (`USER_NOT_FOUND`) → `EmptyState` "Không tìm thấy người dùng" + nút quay lại.
- **API**: `GET /admin/users/{id}` → `AdminUserDetailResponse`.

### 7.12 Admin — Các modal thao tác người dùng
Tất cả dùng `Modal`/`ConfirmDialog` (bottom sheet trên mobile), form có validate, nút loading, hiển thị lỗi server.

| Thao tác | Nội dung | API |
|---|---|---|
| Thêm người dùng | Email, Họ tên, Mật khẩu (tuỳ chọn; bỏ trống = hệ thống tự sinh & gửi mail), Vai trò (multi-select checkbox), Trạng thái, Switch "Email đã xác thực" | `POST /admin/users` |
| Sửa người dùng | Họ tên, username, SĐT, ngày sinh, giới tính (email chỉ đọc) | `PUT /admin/users/{id}` |
| Đổi trạng thái | Radio `ACTIVE/INACTIVE/LOCKED`, ô lý do (tuỳ chọn), cảnh báo "Người dùng sẽ bị đăng xuất khỏi mọi thiết bị" khi chọn LOCKED/INACTIVE | `PATCH /admin/users/{id}/status` |
| Gán vai trò | Danh sách role dạng checkbox card (tên + mô tả + số permission); không cho bỏ ADMIN cuối cùng (hiển thị lỗi `OPERATION_NOT_ALLOWED`) | `PUT /admin/users/{id}/roles` |
| Reset mật khẩu | Ô mật khẩu mới (tuỳ chọn) + ghi chú "để trống sẽ sinh ngẫu nhiên và gửi email" | `POST /admin/users/{id}/reset-password` |
| Buộc đăng xuất | `ConfirmDialog` | `POST /admin/users/{id}/force-logout` |
| Xoá (mềm) | `ConfirmDialog` nguy hiểm, bắt gõ lại email để xác nhận | `DELETE /admin/users/{id}` |
| Khôi phục | `ConfirmDialog` | `POST /admin/users/{id}/restore` |

### 7.13 Admin — Vai trò `/admin/roles`
- Lưới card (1/2/3 cột theo breakpoint): tên role, mô tả, số người dùng, số quyền, icon khoá nếu `systemRole`, nút Sửa/Xoá (role hệ thống: không xoá được, không đổi tên, kèm tooltip).
- Nút "+ Thêm vai trò" mở modal: Tên (`UPPER_SNAKE_CASE`, validate), Mô tả, **ma trận quyền**: các permission nhóm theo `resource` (USER, ROLE, PERMISSION, SESSION), mỗi nhóm là 1 khối có checkbox "Chọn tất cả nhóm" và checkbox từng quyền. Mobile: các nhóm là accordion.
- Xoá role đang có người dùng → hiển thị lỗi `ROLE_IN_USE`.
- **API**: `GET /admin/roles`, `GET /admin/roles/{id}`, `POST /admin/roles`, `PUT /admin/roles/{id}`, `PUT /admin/roles/{id}/permissions`, `DELETE /admin/roles/{id}`.

### 7.14 Admin — Quyền hạn `/admin/permissions`
- Bảng/danh sách card: tên, resource, action, mô tả, số role đang dùng; tìm kiếm phía client; lọc theo resource. Thêm/Sửa/Xoá qua modal (Tên `RESOURCE_ACTION`, Mô tả, Resource, Action). Quyền nằm trong danh sách mặc định của hệ thống (`PredefinedPermission`) hiển thị badge "Hệ thống" và không cho xoá ở UI.
- **API**: `GET/POST /admin/permissions`, `PUT/DELETE /admin/permissions/{id}`.

### 7.15 Trang lỗi & trạng thái toàn cục
- **404**: card giữa trang (số 404 lớn `text-indigo-500`, mô tả, nút "Về trang chủ").
- **403**: icon khoá, "Bạn không có quyền truy cập", nút "Về trang chủ".
- **Hết phiên** (refresh thất bại giữa chừng): toast "Phiên đăng nhập đã hết hạn" + chuyển `/login` (nhớ trang đích).
- **Lỗi mạng**: toast + `ErrorState` có nút Thử lại.
- **Splash khởi động**: `Logo` + spinner cho tới khi silent refresh xong (`isInitialized`).
- `ErrorBoundary` bọc router, hiển thị màn hình lỗi chung thay vì trắng trang.

---

## 8. KIỂU DỮ LIỆU (`src/types/`) — PHẢN CHIẾU DTO BACKEND

Phải khớp với plan (mục 7 và mục 8 của `IMPLEMENTATION_PLAN.md`). Tên trường dùng `camelCase`, ngày giờ là chuỗi ISO-8601.

```ts
// api.ts
export interface ApiResponse<T> { code: number; message?: string; result?: T }
export interface FieldErrorResponse { field: string; message: string }
export interface PageResponse<T> {
  content: T[]; page: number; size: number; totalElements: number;
  totalPages: number; first: boolean; last: boolean;
}

// constants/httpCode.ts: hằng số mã nghiệp vụ, ĐÚNG số trong bảng HTTPCode của plan
export const HTTP_CODE = {
  SUCCESS: 1000, CREATED: 1001,
  VALIDATION_FAILED: 2001,
  UNAUTHENTICATED: 3000, INVALID_CREDENTIALS: 3001, TOKEN_EXPIRED: 3002, TOKEN_INVALID: 3003,
  TOKEN_REVOKED: 3004, REFRESH_TOKEN_MISSING: 3005, ACCOUNT_LOCKED: 3006,
  ACCOUNT_DISABLED: 3007, EMAIL_NOT_VERIFIED: 3008,
  FORBIDDEN: 4000, USER_NOT_FOUND: 5000,
  EMAIL_ALREADY_EXISTS: 6000, /* …đủ các mã còn lại theo bảng trong plan */
  OTP_INVALID: 8000, INTERNAL_ERROR: 9000, TOO_MANY_REQUESTS: 9001, OPERATION_NOT_ALLOWED: 9002,
} as const;

// user.ts
export type EntityStatus = 'ACTIVE' | 'INACTIVE' | 'LOCKED' | 'PENDING_VERIFICATION' | 'DELETED';
export type AuthProvider = 'LOCAL' | 'GOOGLE';
export type Gender = 'MALE' | 'FEMALE' | 'OTHER';

export interface UserProfileResponse {
  id: string; email: string; username?: string; fullName: string; phoneNumber?: string;
  dateOfBirth?: string; gender?: Gender; avatarUrl?: string; authProvider: AuthProvider;
  emailVerified: boolean; roles: string[]; permissions: string[];
  createdAt: string; lastLoginAt?: string;
}
export interface LoginResponse {
  accessToken: string; tokenType: string; expiresIn: number; user: UserProfileResponse;
}
export interface AdminUserListItemResponse {
  id: string; email: string; fullName: string; avatarUrl?: string; roles: string[];
  status: EntityStatus; authProvider: AuthProvider; emailVerified: boolean; createdAt: string;
}
export interface AdminUserDetailResponse extends AdminUserListItemResponse {
  username?: string; phoneNumber?: string; dateOfBirth?: string; gender?: Gender; providerId?: string;
  permissions: string[]; createdBy?: string; modifiedAt?: string; modifiedBy?: string;
  lastLoginAt?: string; passwordChangedAt?: string;
}
export interface PermissionResponse { id: string; name: string; description?: string; resource?: string; action?: string; roleCount?: number; systemPermission?: boolean }
export interface RoleResponse { id: string; name: string; description?: string; systemRole: boolean; permissions: PermissionResponse[]; userCount?: number }
export interface AdminUserStatisticsResponse {
  total: number; byStatus: Record<EntityStatus, number>; byProvider: Record<AuthProvider, number>;
  registrationsLast7Days: { date: string; count: number }[];
  registrationsLast30Days: { date: string; count: number }[];
}
```
Tất cả `XxxRequest`/`XxxResponse` còn lại (mỗi API một cặp, đúng tên như cột "Request/Response" trong plan mục 7) đều phải được khai báo trong `types/`. Không dùng chung một type cho hai API khác nhau.

> Ghi chú: một số trường bổ sung trong type trên (`roleCount`, `userCount`, `systemPermission`) là **đề xuất phục vụ UI**. Đánh dấu chúng bằng comment `// TODO(connect): xác nhận backend trả trường này`, và ghi lại vào danh sách "Yêu cầu bổ sung cho backend" ở mục 12.

---

## 9. LỚP API & MOCK

### 9.1 Nguyên tắc
- `api/contracts.ts` khai báo interface cho từng nhóm API, ví dụ `AuthApi { login(req: LoginRequest): Promise<LoginResponse>; … }`. Hàm **trả về `result` đã bóc vỏ** và **ném `ApiError`** khi `code` khác thành công.
- `ApiError { code: number; message: string; httpStatus?: number; fieldErrors?: FieldErrorResponse[] }`.
- `api/http/*`: cài đặt thật bằng `axiosClient` (baseURL từ config, `withCredentials: true`, gắn `Authorization`, interceptor refresh có hàng đợi, bóc `ApiResponse`, chuyển lỗi thành `ApiError`). **Viết đầy đủ theo bảng endpoint của plan**, dù ở bước này chưa test được với backend.
- `api/mock/*`: cài đặt cùng interface, đọc/ghi `mocks/db.ts`, mô phỏng latency ngẫu nhiên trong khoảng cấu hình, ném đúng `ApiError` với đúng mã.
- `api/index.ts`: `export const authApi = config.USE_MOCK ? mockAuthApi : httpAuthApi` (tương tự các nhóm còn lại). **UI và hook chỉ import từ `api/index.ts`**, không import trực tiếp `http` hay `mock`.

### 9.2 Mô phỏng phiên (chỉ dùng cho mock, xoá ở bước connect)
- Backend thật giữ refresh token trong HttpOnly cookie; ở mock, mô phỏng bằng key `localStorage` **`mock_refresh_session`** (khai báo trong `storageKeys.ts`, ghi chú `MOCK ONLY`). Access token vẫn chỉ nằm trong memory (Zustand).
- `refresh()` mock: có phiên → trả access token mới; không có → ném `REFRESH_TOKEN_MISSING`. Access token mock có hạn ngắn (ví dụ 2 phút, cấu hình) để **kiểm chứng được cơ chế refresh và hàng đợi request** ngay ở mock.
- `logout`/`logoutAll`/đổi mật khẩu/khoá user/force-logout: xoá phiên mock tương ứng.

### 9.3 Quy tắc hành vi mock (dùng để demo & test UI)

| Kịch bản | Cách kích hoạt | Kết quả |
|---|---|---|
| Đăng nhập admin | `admin@example.com` / `Admin@12345` | thành công, role ADMIN, đủ permission |
| Đăng nhập user | `user@example.com` / `User@12345` | role USER, không quyền admin |
| Đăng nhập moderator | `moderator@example.com` / `Mod@12345` | chỉ `USER_READ`, `USER_MANAGE_STATUS` |
| Sai mật khẩu | mật khẩu bất kỳ khác | `INVALID_CREDENTIALS`; sai 5 lần liên tiếp cùng email → `ACCOUNT_LOCKED` |
| Tài khoản bị khoá | `locked@example.com` | `ACCOUNT_LOCKED` |
| Tài khoản vô hiệu | `disabled@example.com` | `ACCOUNT_DISABLED` |
| Chưa xác thực email | `unverified@example.com` | `EMAIL_NOT_VERIFIED` |
| Tài khoản Google | `google.user@example.com` (không có mật khẩu) | chỉ đăng nhập qua nút Google mô phỏng |
| Đăng ký trùng email | email đã có trong db | `EMAIL_ALREADY_EXISTS` |
| OTP đúng | `123456` | thành công, trả `resetToken` |
| OTP hết hạn | `000000` | `OTP_EXPIRED` |
| OTP sai | mã khác | `OTP_INVALID`; sai 5 lần → `OTP_TOO_MANY_ATTEMPTS` |
| Gửi lại OTP quá sớm | trong vòng `OTP_RESEND_SECONDS` | `OTP_RESEND_TOO_SOON` |
| Token xác thực email | `valid-token` hợp lệ; `expired-token` hết hạn; khác → không hợp lệ | tương ứng |
| Đổi mật khẩu | mật khẩu cũ phải khớp mật khẩu mock hiện tại | sai → `OLD_PASSWORD_INCORRECT`; trùng mật khẩu cũ → `NEW_PASSWORD_SAME_AS_OLD` |
| Admin thao tác lên chính mình (khoá/xoá) | — | `OPERATION_NOT_ALLOWED` |
| Gỡ ADMIN cuối cùng | — | `OPERATION_NOT_ALLOWED` |
| Xoá role đang có user / role hệ thống | — | `ROLE_IN_USE` / `OPERATION_NOT_ALLOWED` |
| Lỗi máy chủ ngẫu nhiên | query `?mockError=500` trên URL hiện tại | API kế tiếp ném `INTERNAL_ERROR` (để test `ErrorState`) |
| Danh sách rỗng | `?mockEmpty=1` | API danh sách trả `content: []` |

Mock phải **tôn trọng permission** như backend: gọi API admin khi không đủ quyền → `FORBIDDEN`; chưa đăng nhập → `UNAUTHENTICATED`.

---

## 10. MOCK DATA

Đặt trong `src/mocks/seed/`. Dữ liệu dưới đây là **mẫu bắt buộc tối thiểu**; AI phải mở rộng `users` lên **tối thiểu 30 bản ghi** (sinh bằng hàm, tên tiếng Việt đa dạng, trải đều trạng thái/role/provider/ngày tạo trong 60 ngày gần nhất) để kiểm thử phân trang, tìm kiếm, lọc.

### 10.1 Permissions (`seed/permissions.ts`)
```ts
export const PERMISSIONS: PermissionResponse[] = [
  { id: 'p-01', name: 'USER_READ',           resource: 'USER',       action: 'READ',   description: 'Xem danh sách và chi tiết người dùng', systemPermission: true },
  { id: 'p-02', name: 'USER_CREATE',         resource: 'USER',       action: 'CREATE', description: 'Tạo người dùng mới', systemPermission: true },
  { id: 'p-03', name: 'USER_UPDATE',         resource: 'USER',       action: 'UPDATE', description: 'Cập nhật thông tin, reset mật khẩu', systemPermission: true },
  { id: 'p-04', name: 'USER_DELETE',         resource: 'USER',       action: 'DELETE', description: 'Xoá (mềm) người dùng', systemPermission: true },
  { id: 'p-05', name: 'USER_MANAGE_STATUS',  resource: 'USER',       action: 'MANAGE_STATUS', description: 'Khoá/mở khoá, khôi phục người dùng', systemPermission: true },
  { id: 'p-06', name: 'USER_ASSIGN_ROLE',    resource: 'USER',       action: 'ASSIGN_ROLE',   description: 'Gán vai trò cho người dùng', systemPermission: true },
  { id: 'p-07', name: 'ROLE_READ',           resource: 'ROLE',       action: 'READ',   description: 'Xem vai trò', systemPermission: true },
  { id: 'p-08', name: 'ROLE_CREATE',         resource: 'ROLE',       action: 'CREATE', description: 'Tạo vai trò', systemPermission: true },
  { id: 'p-09', name: 'ROLE_UPDATE',         resource: 'ROLE',       action: 'UPDATE', description: 'Sửa vai trò, gán quyền', systemPermission: true },
  { id: 'p-10', name: 'ROLE_DELETE',         resource: 'ROLE',       action: 'DELETE', description: 'Xoá vai trò', systemPermission: true },
  { id: 'p-11', name: 'PERMISSION_READ',     resource: 'PERMISSION', action: 'READ',   description: 'Xem quyền hạn', systemPermission: true },
  { id: 'p-12', name: 'PERMISSION_CREATE',   resource: 'PERMISSION', action: 'CREATE', description: 'Tạo quyền hạn', systemPermission: true },
  { id: 'p-13', name: 'PERMISSION_UPDATE',   resource: 'PERMISSION', action: 'UPDATE', description: 'Sửa quyền hạn', systemPermission: true },
  { id: 'p-14', name: 'PERMISSION_DELETE',   resource: 'PERMISSION', action: 'DELETE', description: 'Xoá quyền hạn', systemPermission: true },
  { id: 'p-15', name: 'SESSION_REVOKE',      resource: 'SESSION',    action: 'REVOKE', description: 'Buộc đăng xuất người dùng', systemPermission: true },
];
```

### 10.2 Roles (`seed/roles.ts`)
```ts
export const ROLES = [
  { id: 'r-01', name: 'ADMIN',     description: 'Quản trị viên toàn quyền',        systemRole: true,  permissionNames: 'ALL' },
  { id: 'r-02', name: 'MODERATOR', description: 'Kiểm duyệt: xem và quản lý trạng thái người dùng', systemRole: true,  permissionNames: ['USER_READ', 'USER_MANAGE_STATUS'] },
  { id: 'r-03', name: 'USER',      description: 'Người dùng thông thường',          systemRole: true,  permissionNames: [] },
  { id: 'r-04', name: 'SUPPORT',   description: 'Hỗ trợ khách hàng (role tuỳ chỉnh mẫu)', systemRole: false, permissionNames: ['USER_READ', 'USER_UPDATE', 'SESSION_REVOKE'] },
];
```
`userCount`/`roleCount` được **tính động** từ db mock, không ghi cứng.

### 10.3 Users — 8 bản ghi mẫu cố định (`seed/users.ts`), còn lại sinh tự động
```ts
export const SEED_USERS = [
  { id: 'u-001', email: 'admin@example.com',       fullName: 'Nguyễn Quản Trị',  username: 'admin',     phoneNumber: '0901234567', gender: 'MALE',   dateOfBirth: '1990-05-12', authProvider: 'LOCAL',  emailVerified: true,  status: 'ACTIVE',               roles: ['ADMIN'],     password: 'Admin@12345', avatarUrl: 'https://i.pravatar.cc/150?img=12', createdAt: '2026-01-05T08:00:00Z', lastLoginAt: '2026-09-27T21:10:00Z' },
  { id: 'u-002', email: 'moderator@example.com',   fullName: 'Trần Kiểm Duyệt',  username: 'moderator', phoneNumber: '0912345678', gender: 'FEMALE', dateOfBirth: '1993-08-21', authProvider: 'LOCAL',  emailVerified: true,  status: 'ACTIVE',               roles: ['MODERATOR'], password: 'Mod@12345',   createdAt: '2026-02-11T03:20:00Z', lastLoginAt: '2026-09-26T09:45:00Z' },
  { id: 'u-003', email: 'user@example.com',        fullName: 'Lê Văn Người Dùng', username: 'levanuser', phoneNumber: '0923456789', gender: 'MALE',   dateOfBirth: '1998-01-30', authProvider: 'LOCAL',  emailVerified: true,  status: 'ACTIVE',               roles: ['USER'],      password: 'User@12345',  createdAt: '2026-03-02T10:15:00Z', lastLoginAt: '2026-09-28T01:00:00Z' },
  { id: 'u-004', email: 'google.user@example.com', fullName: 'Phạm Thị Google',  authProvider: 'GOOGLE', providerId: '1029384756', emailVerified: true, status: 'ACTIVE', roles: ['USER'], avatarUrl: 'https://i.pravatar.cc/150?img=47', createdAt: '2026-04-18T14:00:00Z', lastLoginAt: '2026-09-25T18:30:00Z' },
  { id: 'u-005', email: 'locked@example.com',      fullName: 'Hoàng Bị Khoá',    authProvider: 'LOCAL',  emailVerified: true,  status: 'LOCKED',               roles: ['USER'],      password: 'Locked@12345', createdAt: '2026-05-09T07:40:00Z' },
  { id: 'u-006', email: 'disabled@example.com',    fullName: 'Vũ Ngừng Hoạt Động', authProvider: 'LOCAL', emailVerified: true, status: 'INACTIVE',             roles: ['USER'],      password: 'Disabled@12345', createdAt: '2026-05-21T11:11:00Z' },
  { id: 'u-007', email: 'unverified@example.com',  fullName: 'Đặng Chưa Xác Thực', authProvider: 'LOCAL', emailVerified: false, status: 'PENDING_VERIFICATION', roles: ['USER'],      password: 'Unverified@12345', createdAt: '2026-09-20T16:05:00Z' },
  { id: 'u-008', email: 'deleted@example.com',     fullName: 'Bùi Đã Xoá',       authProvider: 'LOCAL',  emailVerified: true,  status: 'DELETED',              roles: ['USER'],      password: 'Deleted@12345', createdAt: '2026-06-01T05:00:00Z' },
];
```
- Avatar dùng `https://i.pravatar.cc` chỉ là **dữ liệu mock** (URL do người dùng nhập vốn là dữ liệu, không phải asset của app); bản ghi không có `avatarUrl` phải hiển thị avatar chữ cái. Component `Avatar` phải xử lý ảnh lỗi (`onError` → fallback chữ cái).
- Sinh thêm ≥ 22 user: tên Việt, email `user{n}@example.com`, trộn trạng thái (chủ yếu ACTIVE), ~25% GOOGLE, vài user có role `SUPPORT`/`MODERATOR`, `createdAt` rải trong 60 ngày (dùng cho thống kê 7/30 ngày).
- `createdBy`/`modifiedBy` mock: `SYSTEM` hoặc `admin@example.com`.

### 10.4 Statistics
Tính **động** từ danh sách user trong db mock (tổng, theo status, theo provider, số đăng ký mỗi ngày 7/30 ngày gần nhất), để thao tác CRUD ở UI cập nhật dashboard ngay.

### 10.5 Trạng thái db mock
Giữ trong bộ nhớ (reset khi F5). Có nút ẩn tiện ích dev (chỉ khi `USE_MOCK`): góc dưới trái, mở ra panel nhỏ với "Reset dữ liệu mock" và danh sách tài khoản demo (bấm để điền sẵn form login). Panel này phải nằm sau điều kiện `config.USE_MOCK` để không lọt vào bản build thật.

---

## 11. XỬ LÝ XÁC THỰC PHÍA FRONTEND (theo đúng plan)

- Access token: chỉ trong Zustand (memory). **Cấm** lưu vào localStorage/sessionStorage.
- Khởi động: `initialize()` gọi `refresh()` → nếu thành công gọi `getMe()` → set user; luôn kết thúc bằng `isInitialized = true`.
- Interceptor (bản http và cả cơ chế mock): khi nhận `TOKEN_EXPIRED` → refresh **một lần**, các request đến trong lúc đó xếp hàng chờ rồi replay; refresh thất bại → xoá store, toast "Phiên đăng nhập đã hết hạn", chuyển `/login` kèm `redirectTo`.
- `usePermission()` cung cấp `hasPermission(p)`, `hasRole(r)`, `hasAnyPermission([...])`. Menu, nút, route đều dùng hook này.
- Sau login/đổi mật khẩu/đăng xuất: `queryClient.clear()`.

---

## 12. BÀN GIAO CHO BƯỚC 2 & 3

### 12.1 Khi chạy `IMPLEMENTATION_PLAN.md` (bước 2)
Frontend **đã hoàn thành** ở bước này. Trong plan, các phase frontend (Phase 10, 11, 12 và phần frontend của Phase 13) coi như **đã làm bằng mock**: AI ở bước 2 chỉ tập trung backend, Docker, tài liệu và **không viết lại UI**. Lưu ý riêng: `frontend/skill.md` đã được tạo ở bước này (mục 14).

### 12.2 Checklist connect (bước 3)
1. Đặt `VITE_USE_MOCK=false`, `VITE_API_BASE_URL` trỏ backend (dev: có thể dùng proxy Vite để cookie same-site, cấu hình `server.proxy`).
2. Đối chiếu từng `api/http/*` với Swagger của backend: đường dẫn, method, tên trường, mã lỗi (`HTTP_CODE`) — sửa lệch bên nào cũng phải cập nhật `types/` và `contracts.ts` cùng lúc.
3. Kiểm tra cookie refresh: `withCredentials`, CORS `allow-credentials`, `SameSite`, `path=/api/v1/auth` khớp `VITE_API_BASE_URL`.
4. Xác nhận các trường bổ sung đã đánh dấu `TODO(connect)` (mục 8). **Yêu cầu bổ sung cho backend** dự kiến: `roleCount` (Permission), `userCount` (Role), `systemPermission` (Permission), thống kê theo ngày trong `AdminUserStatisticsResponse`, `AdminUserDetailResponse` gồm `createdBy/modifiedBy/passwordChangedAt`. Nếu backend không trả, chỉnh UI ẩn phần đó (không tự bịa dữ liệu).
5. Xoá mã **MOCK ONLY**: `api/mock/`, `mocks/`, key `mock_refresh_session`, panel dev demo; hoặc giữ nhưng đảm bảo tree-shake khỏi bản production (import động sau `config.USE_MOCK`).
6. Test end-to-end đủ các luồng ở mục 6.4 của plan (register → verify → login → refresh → logout; quên mật khẩu 3 bước; Google; admin CRUD).
7. Cập nhật `frontend/skill.md`: bỏ hướng dẫn mock (hoặc chuyển thành mục "Chế độ dev với mock").

---

## 13. LỘ TRÌNH THỰC HIỆN (mỗi phase phải build + lint pass)

**Phase 0 — Khởi tạo**
- [ ] Vite React TS, Tailwind, thư viện, ESLint/Prettier, alias `@/`.
- [ ] Cấu trúc thư mục mục 4, `.env.example`, `.gitignore` frontend, `constants/*`.

**Phase 1 — Design system**
- [ ] Toàn bộ component ở mục 2.2 + 3 layout ở 2.3.
- [ ] Trang nội bộ `/__ui` (chỉ khi `USE_MOCK`) hiển thị mọi component/variant/trạng thái để soát trực quan ở các độ rộng.

**Phase 2 — Types, API contracts, mock**
- [ ] `types/`, `contracts.ts`, `ApiError`, `mocks/seed`, `mocks/db`, `api/mock/*`, `api/http/*`, `api/index.ts`.
- [ ] `authStore`, `axiosClient` (interceptor + hàng đợi), `initialize()`.

**Phase 3 — Router & guard**
- [ ] `router.tsx`, `ProtectedRoute`, `GuestRoute`, `PermissionGuard`, redirect theo vai trò, `ErrorBoundary`, trang 403/404, splash.

**Phase 4 — Màn hình Auth**
- [ ] 7.1 → 7.7, đủ validate, trạng thái, mock kịch bản.

**Phase 5 — Hồ sơ cá nhân**
- [ ] 7.8 (4 tab).

**Phase 6 — Admin: Dashboard & Người dùng**
- [ ] 7.9 → 7.12.

**Phase 7 — Admin: Vai trò & Quyền hạn**
- [ ] 7.13, 7.14.

**Phase 8 — Hoàn thiện**
- [ ] Rà soát responsive ở 320/360/390/768/1024/1440 cho **từng màn hình** và modal.
- [ ] Rà soát a11y (tab order, focus, label, contrast ≥ 4.5:1 — chú ý `text-gray-400` chỉ dùng cho chữ không quan trọng).
- [ ] Rà soát hard-code (grep `localhost`, `"/admin`, `"ADMIN"`, số thời gian) và loại bỏ.
- [ ] Viết `frontend/skill.md` (+ bản sao `.agent/skills/frontend-conventions/SKILL.md` có frontmatter `name`, `description`).
- ✅ `npm run build`, `npm run lint` pass; `npm run dev` chạy trọn vẹn với `VITE_USE_MOCK=true`.

---

## 14. NỘI DUNG `frontend/skill.md`

Phải có: (1) tổng quan & stack; (2) design system: bảng token và cách dùng từng component (trích mục 1–2); (3) quy tắc responsive (mục 3); (4) cấu trúc thư mục & luật phụ thuộc (page → hook → `api/index.ts`; component UI không gọi API); (5) quy ước constants/types/schemas/messages, cấm hard-code, cấm `any`; (6) cơ chế mock: cách bật/tắt, bảng kịch bản mock, cách thêm kịch bản/API mới (**luôn cập nhật đủ 4 nơi: `types`, `contracts`, `mock`, `http`**); (7) luồng auth phía FE (mục 11); (8) checklist thêm một màn hình mới (route → guard → page → states → responsive → a11y → mock → test); (9) lệnh và Definition of Done; (10) danh sách "KHÔNG ĐƯỢC LÀM" (thư viện UI ngoài, ảnh/icon từ URL ngoài, lưu token vào storage, logic gọi API trong component, style lệch design system như nút vuông/input không bo tròn).

---

## 15. CHECKLIST NGHIỆM THU

**Đồng bộ phong cách**
- [ ] Mọi input/nút/badge/tab đều bo `rounded-full`; card/modal `rounded-xl` với bóng đúng token; màu chính indigo; link xanh gạch chân — trang admin không "lệch tông" so với trang login.
- [ ] Không còn nút Apple; nút Google ghi đúng "Google"; không dùng ảnh từ URL ngoài cho asset của app.

**Đầy đủ màn hình**
- [ ] 7.1 → 7.15 đều có, đủ trạng thái loading/empty/error/success.
- [ ] Mỗi API trong bảng plan mục 7 (38 API) đều có hàm ở `contracts.ts`, bản `mock` và bản `http`, và được UI gọi ở đúng nơi (trừ 2 URL redirect OAuth do Spring xử lý).

**Responsive**
- [ ] Không scroll ngang ở body từ 320px; bảng → card <768; sidebar → drawer <1024; modal → bottom sheet <640; bộ lọc → bottom sheet trên mobile.
- [ ] Vùng chạm ≥44px; input mobile ≥16px.

**Chức năng mock**
- [ ] Mọi kịch bản ở bảng 9.3 hoạt động; refresh/hàng đợi hoạt động (kiểm chứng bằng access token mock ngắn hạn).
- [ ] Phân quyền hiển thị đúng với 3 tài khoản admin/moderator/user.
- [ ] Bộ lọc/sort/trang của danh sách user đồng bộ với URL.
- [ ] Dashboard cập nhật khi CRUD user.

**Chuẩn code**
- [ ] Không `any`, không hard-code, không import trực tiếp `api/mock`/`api/http` từ UI.
- [ ] Có `frontend/skill.md`, `.gitignore`, `.env.example`.
- [ ] `npm run build` và `npm run lint` pass.

---

## 16. NGOÀI PHẠM VI
Dark mode, đa ngôn ngữ (chỉ chuẩn bị chỗ chứa chuỗi), upload file ảnh đại diện (chỉ nhập URL), đăng nhập mạng xã hội khác Google, MFA/2FA, PWA/offline.
