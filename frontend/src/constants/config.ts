export const config = {
  APP_NAME: import.meta.env.VITE_APP_NAME || 'Auth Service',
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  USE_MOCK: import.meta.env.VITE_USE_MOCK === 'true',
  MOCK_LATENCY_MIN_MS: Number(import.meta.env.VITE_MOCK_LATENCY_MIN_MS) || 300,
  MOCK_LATENCY_MAX_MS: Number(import.meta.env.VITE_MOCK_LATENCY_MAX_MS) || 800,
  
  OTP_LENGTH: 6,
  OTP_RESEND_SECONDS: 60,
  PASSWORD_MIN_LENGTH: 8,
  SEARCH_DEBOUNCE_MS: 400,
  DEFAULT_PAGE_SIZE: 10,
  PAGE_SIZE_OPTIONS: [10, 20, 50],
  TOAST_DURATION_MS: 3000,
  ACCESS_TOKEN_REFRESH_SKEW_MS: 60000,
};
