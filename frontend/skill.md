---
name: frontend-conventions
description: Quy ước code, kiến trúc frontend React (auth-service). Dùng để nhắc nhở agent khi code tính năng React.
---

# 1. Tổng quan dự án và tech stack
- **Framework**: React 18, Vite.
- **Ngôn ngữ**: TypeScript.
- **UI/Styling**: TailwindCSS.
- **Router**: React Router v6.
- **Data Fetching/State**: TanStack Query, Axios, Zustand (cho memory token/state cục bộ).
- **Forms**: React Hook Form + Zod.

# 2. Cấu trúc thư mục
- `src/components/`: Reusable components (button, modal, input).
- `src/hooks/`: Custom hooks.
- `src/pages/`: Pages tương ứng với routes.
- `src/api/`: Khai báo Axios, interceptors, functions fetch.
- `src/store/`: Zustand stores.
- `src/constants/`: Constant, routes config.
- `src/types/`: Typescript types/interfaces (Zod schema có thể đặt ở đây hoặc cùng với component).

**Luật phụ thuộc:** Pages -> Hooks -> API. Component không gọi Axios trực tiếp. 

# 3. Quy ước chung
- Không dùng `any`, phải define interface/type hoặc dùng Zod schema infer.
- Các hằng số (API url, routes) không hard-code, để ở file constants.
- Zod schema dùng cho validation. Form errors hiển thị bằng màu hoặc thông báo rõ ràng (Tailwind).

# 4. Luồng Auth (Frontend)
- Access Token lưu trong **Memory** (Zustand store), không lưu ở LocalStorage.
- Refresh Token được backend tự set qua `HttpOnly` cookie.
- Setup Axios Interceptor:
  - Tự động gắn header `Authorization: Bearer <token>`.
  - Nếu báo 401: Cố gắng gọi API `/auth/refresh` một lần, nếu thành công thì lấy token mới cập nhật store và retry request cũ. Nếu thất bại, báo đăng xuất và redirect sang `/login`.
- **Silent Refresh**: Lúc mở ứng dụng, Zustand store chạy luồng gọi refresh API xem còn cookie hợp lệ không.

# 5. Quy trình thêm Trang/API/Form
1. Định nghĩa Type/Zod schema cho Request/Response DTO.
2. Thêm hàm API gọi axios ở `src/api/`.
3. Tạo custom hook với TanStack Query (`useQuery` hoặc `useMutation`).
4. Xây dựng Form (dùng React Hook Form).
5. Add route vào Router config.

# 6. Quy ước UI/Tailwind
- Không code CSS thuần nếu Tailwind làm được.
- Build UI đẹp (vibrant colors, gradients, micro-animations hover).
- Trạng thái loading, error (skeleton, spin) phải có đủ.

# 7. Lệnh và Biến Môi Trường
- Lệnh: `npm run dev` (chạy), `npm run build` (prod), `npm run lint`.
- Biến: `VITE_API_BASE_URL`, `VITE_GOOGLE_CLIENT_ID`.

# 8. Anti-patterns
- Lưu access token vào local storage (không an toàn).
- Call api trong useEffect mà không handle race condition hoặc không dùng react-query.
- Props drilling quá đà (dùng Zustand để thay thế).
