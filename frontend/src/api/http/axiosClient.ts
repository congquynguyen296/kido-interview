import { useAuthStore } from '../../store/authStore';
import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { HTTP_CODE } from '@/constants/httpCode';
import { ApiError } from '../ApiError';

export const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

// For interceptor token handling
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: any) => void;
}> = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

axiosClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = useAuthStore.getState().accessToken;
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    config.withCredentials = true; // IMPORTANT: For HttpOnly cookies
    return config;
  },
  (error) => Promise.reject(error)
);

axiosClient.interceptors.response.use(
  (response) => {
    // API contract dictates standard response wrap
    const data = response.data;
    if (data && data.code && data.code !== HTTP_CODE.SUCCESS && data.code !== HTTP_CODE.CREATED) {
      throw new ApiError(data.code, data.message || 'API Error', response.status, data.fieldErrors);
    }
    return data.result;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            return axiosClient(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // Call refresh token API. HttpOnly cookie is sent automatically because of withCredentials
        const response = await axios.post(`${axiosClient.defaults.baseURL}/auth/refresh`, {}, {
          withCredentials: true
        });

        const { accessToken } = response.data.result;
        
        useAuthStore.getState().setAccessToken(accessToken);
        
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        }
        
        processQueue(null, accessToken);
        return axiosClient(originalRequest);
      } catch (err) {
        processQueue(err, null);
        useAuthStore.getState().logout();
        return Promise.reject(err);
      } finally {
        isRefreshing = false;
      }
    }

    // Wrap in ApiError
    if (error.response?.data) {
      const data = error.response.data as any;
      throw new ApiError(
        data.code || HTTP_CODE.INTERNAL_ERROR,
        data.message || 'An error occurred',
        error.response.status,
        data.fieldErrors
      );
    }

    throw new ApiError(HTTP_CODE.INTERNAL_ERROR, error.message);
  }
);
